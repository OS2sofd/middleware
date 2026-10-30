package dk.digitalidentity.brugertjek.integration.sofd;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import dk.digitalidentity.brugertjek.dao.municipality.Municipality;
import dk.digitalidentity.brugertjek.integration.Validation;
import dk.digitalidentity.brugertjek.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SofdValidationService {
	private final String SOFD_CORE_VALIDATION_LABEL = "OS2sofd";
	private final SofdService sofdService;

	public Person getPerson(Municipality municipality, String userId, String kombitUuid) {
		try {
			Person person = null;

			if (StringUtils.hasText(userId)) {
				person = sofdService.getPersonByUserId(municipality, userId);
			}
			else {
				person = sofdService.getPersonByKombitUuid(municipality, kombitUuid);
			}
			
			if (person != null && person.getAffiliations() != null) {
				for (Affiliation affiliation : person.getAffiliations()) {
					affiliation.setOrgUnitName(sofdService.getOrgUnitName(municipality, affiliation.getOrgUnitUuid()));
				}
			}
			
			return person;
		}
		catch (Exception ex) {
			log.warn("Could not connect to OS2sofd", ex);

			return null;
		}
	}
	
	public List<Validation> validate(Municipality municipality, String userId, Person person) {
		if (person == null) {
			return Collections.singletonList(new Validation(SOFD_CORE_VALIDATION_LABEL, Constants.STATUS_ERROR, "Personen der har " + userId + " blev ikke fundet"));
		}

		List<Validation> validations = new ArrayList<>();

		if (person.isDeleted()) {
			validations.add(new Validation(SOFD_CORE_VALIDATION_LABEL, Constants.STATUS_ERROR, "Personen der har " + userId + " er slettet"));
		}
		else if (person.getUsers() != null && person.getUsers().size() > 0) {
			SofdUser sofdUser = person.getUsers().stream()
				.filter(u -> Objects.equals(u.getUserId().toLowerCase(), userId.toLowerCase()) && Objects.equals("ACTIVE_DIRECTORY", u.getUserType()))
				.findFirst()
				.orElse(null);

			// should not happen - we just found the person through an API call using the userId
			if (sofdUser == null) {
				validations.add(new Validation(SOFD_CORE_VALIDATION_LABEL, Constants.STATUS_ERROR, "Kontoen " + userId + " findes ikke"));				
			}
			else {
				if (sofdUser.getDisabled() != null && sofdUser.getDisabled() == true) {
					validations.add(new Validation(SOFD_CORE_VALIDATION_LABEL, Constants.STATUS_ERROR, "Kontoen " + userId + " er spærret"));									
				}
				else {
					if (sofdUser.getPasswordExpireDate() != null && !sofdUser.getPasswordExpireDate().equals("1970-01-01")) {
						try {
							LocalDate expireDate = LocalDate.parse(sofdUser.getPasswordExpireDate());
							if (expireDate.isBefore(LocalDate.now())) {
								validations.add(new Validation(SOFD_CORE_VALIDATION_LABEL, Constants.STATUS_ERROR, "Kodeordet på kontoen " + userId + " er udløbet"));
							}
							else if (expireDate.isBefore(LocalDate.now().plusDays(7))) {
								validations.add(new Validation(SOFD_CORE_VALIDATION_LABEL, Constants.STATUS_WARNING, "Kodeordet på kontoen " + userId + " udløber snart"));
							}
						}
						catch (Exception ex) {
							log.warn(municipality.getName() + " : Cannot parse expiryDate for " + userId + " : " + sofdUser.getPasswordExpireDate());
						}
					}
					
					if (sofdUser.getAccountExpireDate() != null && !sofdUser.getAccountExpireDate().equals("1970-01-01")) {
						try {
							LocalDate expireDate = LocalDate.parse(sofdUser.getAccountExpireDate());
							if (expireDate.isBefore(LocalDate.now())) {
								validations.add(new Validation(SOFD_CORE_VALIDATION_LABEL, Constants.STATUS_ERROR, "Kontoen " + userId + " er udløbet"));
							}
							else if (expireDate.isBefore(LocalDate.now().plusDays(7))) {
								validations.add(new Validation(SOFD_CORE_VALIDATION_LABEL, Constants.STATUS_WARNING, "Kontoen " + userId + " udløber snart"));
							}
						}
						catch (Exception ex) {
							log.warn(municipality.getName() + " : Cannot parse expiryDate for " + userId + " : " + sofdUser.getAccountExpireDate());
						}
					}
				}
			}
		}

		// all is okay :)
		if (validations.size() == 0) {
			validations.add(new Validation(SOFD_CORE_VALIDATION_LABEL, Constants.STATUS_SUCCESS, userId + " blev fundet"));
		}

		return validations;
	}
}
