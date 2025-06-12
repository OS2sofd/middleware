package dk.digitalidentity.sofd.logbuy.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import dk.digitalidentity.sofd.logbuy.config.SofdConfiguration;
import dk.digitalidentity.sofd.logbuy.dao.model.CreatedPerson;
import dk.digitalidentity.sofd.logbuy.dao.model.enums.Gender;
import dk.digitalidentity.sofd.logbuy.dao.model.enums.Status;
import dk.digitalidentity.sofd.logbuy.service.model.Person;
import dk.digitalidentity.sofd.logbuy.service.model.PersonCreateRequest;
import dk.digitalidentity.sofd.logbuy.service.model.PersonCreateResponseWrapper;
import dk.digitalidentity.sofd.logbuy.service.model.PersonDeleteRequest;
import dk.digitalidentity.sofd.logbuy.service.model.PersonDeleteResponseWrapper;
import dk.digitalidentity.sofd.logbuy.service.model.TokenResponse;
import dk.digitalidentity.sofd.logbuy.service.model.User;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SyncService {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private SOFDService sofdService;

	@Autowired
	private SofdConfiguration config;

	@Autowired
	private CreatedPersonService createdPersonService;

	@Transactional
	public void execute() {
		try {
			List<Person> persons = sofdService.getPersons();
			List<String> personUuids = persons.stream().map(p -> p.getUuid()).collect(Collectors.toList());

			String token = getToken();
			if (token == null) {
				return;
			}

			for (Person person : persons) {
				CreatedPerson createdPerson = createdPersonService.getByUuid(person.getUuid());
				if (createdPerson == null || createdPerson.getStatus() == Status.DELETED) {
					// create
					if (createdPerson == null) {
						createdPerson = new CreatedPerson();
					}
					User user = person.getUsers().stream()
							.filter(u -> u.getUserType().equals("EXCHANGE") && u.isPrime()).findAny().orElse(null);
					if (user == null) {
						continue;
					}
					createdPerson.setUuid(person.getUuid());
					createdPerson.setFirstName(person.getFirstname());
					createdPerson.setSurName(person.getSurname());
					createdPerson.setEmail(user.getUserId());
					createdPerson.setSalaryNumber(person.getUuid());
					createdPerson.setGender(getGender(person.getCpr()));

					boolean success = createPerson(createdPerson, token);
					createdPerson.setStatus(success ? Status.CREATED : Status.CREATE_FAILED);
					createdPersonService.save(createdPerson);
				}
			}

			// Delete
			List<CreatedPerson> toBeDeleted = createdPersonService.getAll().stream()
					.filter(c -> c.getStatus() == Status.CREATED && !personUuids.contains(c.getUuid()))
					.collect(Collectors.toList());
			for (CreatedPerson personToBeDeleted : toBeDeleted) {
				boolean success = deletePerson(personToBeDeleted, token);
				personToBeDeleted.setStatus(success ? Status.DELETED : Status.DELETE_FAILED);
				createdPersonService.save(personToBeDeleted);
			}

		} catch (Exception ex) {
			log.error("Failed to synchronize logbuy ", ex);
		}

		log.info("Finished synchronizing to LogBuy");
	}

	private String getToken() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("grant_type", "accessbykey");
		map.add("accesskey", config.getLogBuyApiKey());

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
		String url = config.getLogBuyUrl();
		String query = "/token";

		try {
			ResponseEntity<TokenResponse> response = restTemplate.exchange(url + query, HttpMethod.POST, request,
					TokenResponse.class);
			if (response.getStatusCodeValue() != 200) {
				log.error("Could not get token from LogBuy. HTTP " + response.getStatusCodeValue());
			} else {
				if (response.getBody() != null) {
					return response.getBody().getAccess_token();
				} else {
					log.error("Could not get token from LogBuy. Response body was null.");
				}
			}
		} catch (HttpStatusCodeException ex) {
			log.error("Could not get token from LogBuy. HTTP " + ex.getRawStatusCode());
		}

		return null;
	}

	private boolean createPerson(CreatedPerson createdPerson, String token) {
		PersonCreateRequest person = new PersonCreateRequest(createdPerson);
		List<PersonCreateRequest> persons = new ArrayList<>();
		persons.add(person);

		HttpEntity<List<PersonCreateRequest>> request = new HttpEntity<>(persons, getHeaders(token));
		String url = config.getLogBuyUrl();
		String query = "/api/users/import?customerId=" + config.getCustomerId();

		try {
			ResponseEntity<PersonCreateResponseWrapper> response = restTemplate.exchange(url + query, HttpMethod.POST,
					request, PersonCreateResponseWrapper.class);
			if (response.getStatusCodeValue() != 200) {
				log.error("Could not create persons in LogBuy. HTTP " + response.getStatusCodeValue());
			} else {
				if (response.getBody() != null && response.getBody().getResult() != null) {
					if (response.getBody().getResult().getSuccess() == 1
							&& response.getBody().getResult().getTotalCount() == 1
							&& response.getBody().getResult().getErrors() == 0) {
						log.info("Created person with uuid " + createdPerson.getUuid() + " in LogBuy.");
						return true;
					} else {
						if (response.getBody().getResult().getErrorDetails() != null
								&& !response.getBody().getResult().getErrorDetails().isEmpty()) {
							log.error("Creation of person with uuid " + createdPerson.getUuid()
									+ " in LogBuy failed. Error details: "
									+ response.getBody().getResult().getErrorDetails().get(0));
						} else {
							log.error("Creation of person with uuid " + createdPerson.getUuid() + " in LogBuy failed.");
						}
					}
				} else {
					log.error("Creation of person with uuid " + createdPerson.getUuid()
							+ " in LogBuy failed. Response body was null.");
				}
			}
		} catch (HttpStatusCodeException ex) {
			log.error("Could not create person with uuid " + createdPerson.getUuid() + " in LogBuy. HTTP "
					+ ex.getRawStatusCode());
		}

		return false;
	}

	private boolean deletePerson(CreatedPerson personToDelete, String token) {
		List<PersonDeleteRequest> persons = new ArrayList<>();
		PersonDeleteRequest person = new PersonDeleteRequest(personToDelete);
		persons.add(person);

		HttpEntity<List<PersonDeleteRequest>> request = new HttpEntity<>(persons, getHeaders(token));
		String url = config.getLogBuyUrl();
		String query = "/api/users/deleteUsersBySalaryNumbers?customerId=" + config.getCustomerId();

		try {
			ResponseEntity<PersonDeleteResponseWrapper> response = restTemplate.exchange(url + query, HttpMethod.POST,
					request, PersonDeleteResponseWrapper.class);
			if (response.getStatusCodeValue() != 200) {
				log.error("1Could not delete persons in LogBuy. HTTP " + response.getStatusCodeValue());
			} else {
				if (response.getBody() != null && response.getBody().getResult() != null) {
					if (response.getBody().getResult().getProcessedSuccessfully() == 1
							&& response.getBody().getResult().getErrors() == 0) {
						log.info("Deleted person with uuid " + personToDelete.getUuid() + " in LogBuy");
						return true;
					} else {
						log.error("2Deletion of person with uuid " + personToDelete.getUuid() + " in LogBuy failed.");
					}

				} else {
					log.error("3Could not delete person with uuid " + personToDelete.getUuid()
							+ " in LogBuy. Response body was null.");
				}
			}
		} catch (Exception ex) {
			log.warn("4Could not delete person with uuid " + personToDelete.getUuid() + " in LogBuy", ex);
		}

		return false;
	}

	private HttpHeaders getHeaders(String token) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + token);
		headers.add("Content-Type", "application/json");
		return headers;
	}

	private Gender getGender(String cpr) {
		try {
			int lastNumberInCpr = Integer.parseInt(cpr.charAt(cpr.length() - 1) + "");
			if (lastNumberInCpr % 2 == 0) {
				// even
				return Gender.FEMALE;
			} else {
				// odd
				return Gender.MALE;
			}
		} catch (NumberFormatException e) {
			return Gender.UNKNOWN;
		}
	}
}
