package dk.digitalidentity.service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dk.digitalidentity.config.NexusSyncConfiguration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmailService {

	@Autowired
	private NexusSyncConfiguration configuration;
	
	public void sendMessage(String[] emails, String subject, String message) throws UnsupportedEncodingException, MessagingException {
		if (!configuration.isEmailEnabled()) {
			log.warn("email server is not configured - not sending email to " + String.join(",",emails));
			return;
		}

		Transport transport = null;

		log.info("Sending email: '" + subject + "' to " + String.join(",",emails));

		try {
			Properties props = System.getProperties();
			props.put("mail.transport.protocol", "smtps");
			props.put("mail.smtp.port", 25);
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.starttls.required", "true");
			Session session = Session.getDefaultInstance(props);

			List<InternetAddress> result = new ArrayList<>();

			for (String email : emails) {
				try {
					if (!email.trim().isEmpty()) {
						result.add(new InternetAddress(email.trim()));
					}
				}
				catch (AddressException e) {
					log.warn("Unable to parse recipient: " + email);
				}
			}

			InternetAddress[] recipients = result.stream().toArray(InternetAddress[]::new);
			
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress("no-reply@digital-identity.dk", "SOFD Nexus Sync"));
			msg.setRecipients(Message.RecipientType.TO, recipients);
			msg.setSubject(subject, "UTF-8");
			msg.setText(message, "UTF-8");
			msg.setHeader("Content-Type", "text/html; charset=UTF-8");

			transport = session.getTransport();
			transport.connect(configuration.getEmailHost(), configuration.getEmailUsername(), configuration.getEmailPassword());
			transport.addTransportListener(new TransportErrorHandler());
			transport.sendMessage(msg, msg.getAllRecipients());
		}
		finally {
			try {
				if (transport != null) {
					transport.close();
				}
			}
			catch (Exception ex) {
				log.warn("Error occured while trying to terminate connection", ex);
			}
		}
	}
}
