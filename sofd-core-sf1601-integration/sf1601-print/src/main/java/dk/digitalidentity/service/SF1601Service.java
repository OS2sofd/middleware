package dk.digitalidentity.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import dk.digitalidentity.config.SF1601PrintConfiguration;
import dk.digitalidentity.controller.api.ApiController.AttachmentDTO;
import dk.digitalidentity.service.enums.Status;
import dk.digst.digital.post.memolib.builder.AdditionalDocumentBuilder;
import dk.digst.digital.post.memolib.builder.FileBuilder;
import dk.digst.digital.post.memolib.builder.FileContentBuilder;
import dk.digst.digital.post.memolib.builder.MainDocumentBuilder;
import dk.digst.digital.post.memolib.builder.MessageBodyBuilder;
import dk.digst.digital.post.memolib.builder.MessageBuilder;
import dk.digst.digital.post.memolib.builder.MessageHeaderBuilder;
import dk.digst.digital.post.memolib.builder.RecipientBuilder;
import dk.digst.digital.post.memolib.builder.SenderBuilder;
import dk.digst.digital.post.memolib.model.AdditionalDocument;
import dk.digst.digital.post.memolib.model.Message;
import dk.digst.digital.post.memolib.model.MessageBody;
import dk.digst.digital.post.memolib.model.MessageHeader;
import dk.digst.digital.post.memolib.model.MessageType;
import dk.digst.digital.post.memolib.writer.MeMoStreamWriter;
import dk.digst.digital.post.memolib.writer.MeMoWriterFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@EnableScheduling
public class SF1601Service {
	private static final String MEMO_XML_PLACEHOLDER = "{MEMO_XML_PLACEHOLDER}";
	private static final String KOMBI_REQUEST_WRAPPER =
			"<?xml version=\"1.0\"?>" + 
    		"<kombi_request>" + 
    			"<KombiValgKode>Digital Post</KombiValgKode>" +
    			MEMO_XML_PLACEHOLDER +
    		"</kombi_request>";

	private static Map<String, TokenDTO> kombipostafsendTokenMap = new HashMap<String, TokenDTO>();
	private static Map<String, TokenDTO> postforespoergTokenMap = new HashMap<String, TokenDTO>();

	@Autowired
	private SF1601PrintConfiguration config;
	
	@Autowired
	private RestTemplate restTemplate;
	
	record TokenDTO(String samlToken, LocalDateTime samlTokenTts, String accessToken, LocalDateTime accessTokenTts) {}
	
	// refresh accessTokens every 30 minutes - do not log errors during refresh, just warns
	@Scheduled(fixedDelay = 30 * 60 * 1000)
	public void refreshAccessTokens() {
		for (String key : kombipostafsendTokenMap.keySet()) {
			// force-refresh of accessToken, but keep SAML token if possible - silent operation
			getAccessToken(key, kombipostafsendTokenMap, "http://entityid.kombit.dk/service/kombipostafsend/1", true, false, true);
		}

		for (String key : postforespoergTokenMap.keySet()) {
			// force-refresh of accessToken, but keep SAML token if possible - silent operation
			getAccessToken(key, postforespoergTokenMap, "http://entityid.kombit.dk/service/postforespoerg/1", true, false, true);
		}
	}
	
	public Status sendLetter(String cpr, String cvr, String municipalityName, String subject, String content, AttachmentDTO[] attachments) {
		String kombipostafsendAccessToken = getAccessToken(cvr, kombipostafsendTokenMap, "http://entityid.kombit.dk/service/kombipostafsend/1");
		if (kombipostafsendAccessToken == null) {
			log.error("Could not get an accessToken for municipality " + cvr + " for service http://entityid.kombit.dk/service/kombipostafsend/1 when sending message to cpr=" + maskCpr(cpr));
			return Status.FAILED;
		}
		
		String postforespoergAccessToken = getAccessToken(cvr, postforespoergTokenMap, "http://entityid.kombit.dk/service/postforespoerg/1");
		if (postforespoergAccessToken == null) {
			log.error("Could not get an accessToken for municipality " + cvr + " for service http://entityid.kombit.dk/service/postforespoerg/1 when sending message to cpr=" + maskCpr(cpr));
			return Status.FAILED;
		}
		
		switch (getSubscriptionStatus(cpr, postforespoergAccessToken, cvr)) {
			case NOT_SUBSCRIBED:
				log.info("Could not send message to cpr=" + maskCpr(cpr) + " for cvr=" + cvr + " because person not subscribed");
				return Status.NOT_REGISTERED;	
			case TECHNICAL_ISSUE:
				log.warn("Failed to check subscription status for cpr=" + maskCpr(cpr) + " for cvr=" + cvr + ", attempting to send anyway");
				break;
			case SUBSCRIBED:
				break;
		}
		
		String xml = getXml(cpr, cvr, municipalityName, subject, content, attachments);
		if (xml == null) {
			log.error("Failed to generate xml for message for cvr=" + cvr + " for message to cpr=" + maskCpr(cpr));
			return Status.FAILED;
		}

		boolean sendPost = sendPost(kombipostafsendAccessToken, xml, cpr, cvr);
		if (sendPost) {
			log.info("Message with subject=" + subject + " from cvr=" + cvr + " to cpr=" + maskCpr(cpr) + " sent");
			return Status.OK;
		}

		log.error("Failed to send message with subject=" + subject + " from cvr=" + cvr + " to cpr=" + maskCpr(cpr));
		return Status.FAILED;
	}

	private String maskCpr(String cpr) {
		if (cpr == null) {
			return null;
		}
		
		if (cpr.length() != 10) {
			return cpr;
		}
		
		return cpr.substring(0, 6) + "-XXXX";
	}

	private String getXml(String cpr, String cvr, String municipalityName, String subject, String content, AttachmentDTO[] attachments) {
		MessageHeader messageHeader = MessageHeaderBuilder.newBuilder()
				.messageType(MessageType.DIGITALPOST)
				.messageUUID(UUID.randomUUID())
				.label(subject)
				.mandatory(false)
				.legalNotification(false)
				.sender(SenderBuilder.newBuilder()
						.senderId(cvr)
						.idType("CVR")
						.label(municipalityName)
						.build())
				.recipient(RecipientBuilder.newBuilder()
						.recipientId(cpr)
						.idType("CPR")
						.build())
				.postType("VIRKSOMHEDSPOST")
				.build();
		
		MessageBody messageBody = MessageBodyBuilder.newBuilder()
				.createdDateTime(LocalDateTime.now())
				.mainDocument(MainDocumentBuilder.newBuilder()
						.addFile(FileBuilder.newBuilder()
								.encodingFormat("application/pdf")
								.filename(subject.replace(" ", "_") + ".pdf")
								.inLanguage("da")
								.content(FileContentBuilder.newBuilder()
										.base64Content(content)
										.build())
								.build())
						.build())
				.build();
		
		if (attachments != null && attachments.length > 0) {
			List<AdditionalDocument> additionalDocuments = new ArrayList<>();
			
			for (AttachmentDTO attachment : attachments) {
				AdditionalDocument additionalDocument = AdditionalDocumentBuilder.newBuilder()
						.addFile(FileBuilder.newBuilder()
								.encodingFormat("application/pdf")
								.filename(attachment.filename().replace(" ", "_") + ".pdf")
								.inLanguage("da")
								.content(FileContentBuilder.newBuilder()
										.base64Content(attachment.content())
								.build())
						.build())
					.build();
				
				additionalDocuments.add(additionalDocument);
			}

			messageBody.setAdditionalDocument(additionalDocuments);
		}
		
		Message message = MessageBuilder.newBuilder()
		  .messageHeader(messageHeader)
		  .messageBody(messageBody).build();
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		MeMoStreamWriter writer = MeMoWriterFactory.createWriter(output);

		try {
			writer.write(message);
			writer.closeStream();
		}
		catch (IOException e) {
			log.error("Failed to create MeMo message for Eboks message with subject: " + subject + " from cvr: " + cvr);
			return null;
		}
		
		String memoXML = output.toString(Charset.defaultCharset()).replace("<?xml version='1.0' encoding='UTF-8'?>", "");

		return KOMBI_REQUEST_WRAPPER.replace(MEMO_XML_PLACEHOLDER, memoXML);
	}

	private String getAccessToken(String cvr, Map<String, TokenDTO> tokenMap, String endpointReference) {
		return getAccessToken(cvr, tokenMap, endpointReference, false, false, false);
	}
	
	private String getAccessToken(String cvr, Map<String, TokenDTO> tokenMap, String endpointReference, boolean forceNewAccessToken, boolean forceNewSAMLToken, boolean silent) {
		LocalDateTime now = LocalDateTime.now();

		if (tokenMap.containsKey(cvr)) {
			TokenDTO dto = tokenMap.get(cvr);

			if (forceNewAccessToken == true || dto.accessToken() == null || dto.accessTokenTts().plusMinutes(40).isBefore(now)) {

				// accessToken is expired, request new
				if (forceNewSAMLToken == true || dto.samlToken() == null || dto.samlTokenTts().plusHours(4).isBefore(now)) {

					// samlToken is expired, request new
					String samlToken = getSamlToken(cvr, endpointReference);
					String accessToken = requestAccessToken(dto.samlToken, cvr, silent);
					tokenMap.put(cvr, new TokenDTO(samlToken, now, accessToken, now));
					return accessToken;
				}
				else {
					String accessToken = requestAccessToken(dto.samlToken, cvr, silent);
					tokenMap.put(cvr, new TokenDTO(dto.samlToken(), dto.samlTokenTts(), accessToken, now));
					return accessToken;
				}
			}
			else {
				return dto.accessToken();
			}
		}
		else {
			String samlToken = getSamlToken(cvr, endpointReference);
			String accessToken = requestAccessToken(samlToken, cvr, silent);
			TokenDTO tokenDTO = new TokenDTO(samlToken, now, accessToken, now);
			tokenMap.put(cvr, tokenDTO);
			
			return accessToken;
		}
	}
	
	record AnvenderKontekst(String cvr) {}
	record EndpointReference(String address) {}
	record AppliesTo(EndpointReference endpointReference) {}
	record SamlTokenRequest(String tokenType, String requestType, String keyType, AnvenderKontekst anvenderKontekst, String useKey, AppliesTo appliesTo, String onBehalfOf) {}
	record RequestedSecurityToken(String Assertion) {}
	record SamlTokenResponse(RequestedSecurityToken RequestedSecurityToken) {}
	public String getSamlToken(String cvr, String endpointReference) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("content-type", "application/json");
		String url = config.getGetSamlTokenUrl();

		SamlTokenRequest requestObj = new SamlTokenRequest("http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0", 
															"http://docs.oasis-open.org/ws-sx/ws-trust/200512/Issue", 
															"http://docs.oasis-open.org/ws-sx/ws-trust/200512/PublicKey", 
															new AnvenderKontekst(cvr), 
															config.getKeystore().getBase64EncodedCertificate(), 
															new AppliesTo(new EndpointReference(endpointReference)), 
															"");
		String samlToken = null;
		
		try {
			HttpEntity<SamlTokenRequest> request = new HttpEntity<SamlTokenRequest>(requestObj, headers);
			ResponseEntity<SamlTokenResponse> response = restTemplate.exchange(url, HttpMethod.POST, request, SamlTokenResponse.class);

			if (response.getStatusCodeValue() == 200) {
				samlToken = response.getBody().RequestedSecurityToken().Assertion();
			}
			else {
				log.error("requestSamlToken for cvr=" + cvr + " got response: " + response.toString());
			}
		}
		catch (Exception ex) {
			log.error("Failed to get SAML token from SAML token for cvr=" + cvr, ex);
		}
		
		log.info("Fetched SAML token for cvr=" + cvr + " / " + endpointReference);
		
		return samlToken;
	}
	
	public String requestAccessToken(String samlToken, String cvr) {
		return requestAccessToken(samlToken, cvr, false);
	}

	record AccessTokenResponse(String access_token) {}
	public String requestAccessToken(String samlToken, String cvr, boolean silent) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/x-www-form-urlencoded");
		
		String url = config.getServicePlatformenBaseUrl() + "/service/AccessTokenService_1/token";
		String accessToken = null;
		
		try {
			MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
			map.add("saml-token", samlToken);

			HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
			ResponseEntity<AccessTokenResponse> response = restTemplate.exchange(url, HttpMethod.POST, request, AccessTokenResponse.class);

			if (response.getStatusCodeValue() == 200) {
				accessToken = response.getBody().access_token();
			}
			else {
				if (silent) {
					log.warn("requestAccessToken for cvr=" + cvr + " got response: " + response.toString());
				}
				else {
					log.error("requestAccessToken for cvr=" + cvr + " got response: " + response.toString());
				}
			}
		}
		catch (Exception ex) {
			if (silent) {
				log.warn("Failed to get access token from SAML token for cvr=" + cvr, ex);
			}
			else {
				log.error("Failed to get access token from SAML token for cvr=" + cvr, ex);
			}
		}
		
		return accessToken;
	}
	
	public enum SubscriptionStatus { SUBSCRIBED, NOT_SUBSCRIBED, TECHNICAL_ISSUE }
	record SubscriptionResponse(boolean result) {}
	public SubscriptionStatus getSubscriptionStatus(String cpr, String accessToken, String cvr) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/json");
		headers.add(HttpHeaders.AUTHORIZATION, "Holder-of-key " + accessToken);
		headers.add("x-TransaktionsId", UUID.randomUUID().toString());
		//headers.add("x-TransaktionsTid", "2022-05-10T07:29:12Z");
		headers.add("x-TransaktionsTid", getUTCDateTime());
		
		String url = config.getServicePlatformenBaseUrl() + "/service/PostForespoerg_1/digitalpost?cprNumber=" + cpr;
		
		try {
			HttpEntity<SubscriptionResponse> request = new HttpEntity<SubscriptionResponse>(headers);
			ResponseEntity<SubscriptionResponse> response = restTemplate.exchange(url, HttpMethod.GET, request, SubscriptionResponse.class);

			if (response.getStatusCodeValue() == 200) {
				return (response.getBody().result() ? SubscriptionStatus.SUBSCRIBED : SubscriptionStatus.NOT_SUBSCRIBED);
			}
		}
		catch (Exception ex) {
			log.error("Failed to get subscription status for cpr=" + maskCpr(cpr) + " for cvr=" + cvr, ex);
		}
		
		return SubscriptionStatus.TECHNICAL_ISSUE;
	}
	
	record SendPostResponse() {}
	public boolean sendPost(String accessToken, String xml, String cpr, String cvr) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/xml;charset=UTF-8");
		headers.add(HttpHeaders.AUTHORIZATION, "Holder-of-key " + accessToken);
		headers.add("x-TransaktionsId", UUID.randomUUID().toString());
		headers.add("x-TransaktionsTid", getUTCDateTime());
		
		String url = config.getServicePlatformenBaseUrl() + "/service/KombiPostAfsend_1/kombi";
		
		try {
			HttpEntity<String> request = new HttpEntity<String>(xml, headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

			if (response.getStatusCodeValue() == 200) {
				return true;
			}

			log.error("Failed to send post to cpr= " + maskCpr(cpr) + " for cvr=" + cvr + ". Reason=" + response.getBody());
		}
		catch (Exception ex) {
			log.error("Failed to send post to cpr=" + maskCpr(cpr) + " for cvr=" + cvr, ex);
		}
		
		return false;
	}
	
	public String getUTCDateTime() {
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	    return f.format(new Date()) + "Z";
	}
}
