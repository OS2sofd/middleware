package dk.digitalidentity.sofd.wizkids.service;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import dk.digitalidentity.sofd.wizkids.dao.model.Municipality;
import dk.digitalidentity.sofd.wizkids.service.sofd.Person;
import dk.digitalidentity.sofd.wizkids.service.sofd.SOFDService;
import dk.digitalidentity.sofd.wizkids.service.sofd.User;
import dk.digitalidentity.sofd.wizkids.service.wizkid.WizkidService;
import dk.digitalidentity.sofd.wizkids.service.wizkid.WizkidUser;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SyncService {
	
	@Autowired
	private WizkidService wizkidService;

	@Autowired
	private SOFDService sofdService;

	public void sync(Municipality municipality) throws Exception {
		long addCount = 0, createCount = 0, removeCount = 0, updateCount = 0;
		
		List<WizkidUser> users = wizkidService.getUsers(municipality);
		log.info(municipality.getName() + " : Found " + users.size() + " users in Wizkids");

		List<Person> persons = sofdService.getPersons(municipality);
		log.info(municipality.getName() + " : Found " + persons.size() + " persons in OS2sofd");

		// add/remove cases on existing persons
		for (Person person : persons) {
			boolean foundWizkidUser = false;
			
			for (WizkidUser wuser : users) {
				if (Objects.equals(person.getCpr(), wuser.getCivicNumber())) {
					foundWizkidUser = true;

					boolean found = false;

					// check if already created
					User wizkidUser = null, wizkidEmailUser = null;
					for (User user : person.getUsers()) {
						if (!user.getUserType().equals("ACTIVE_DIRECTORY_SCHOOL") && !user.getUserType().equals("SCHOOL_EMAIL")) {
							continue;
						}
						
						if (user.getUserType().equals("ACTIVE_DIRECTORY_SCHOOL") && user.getMaster().equals("Wizkids") && user.getMasterId().equals(wuser.getId())) {
							found = true;
							wizkidUser = user;
						}
						
						// this is a bit of a hack, as we assume there is ever only ONE email address
						if (user.getUserType().equals("SCHOOL_EMAIL") && user.getMaster().equals("Wizkids")) {
							wizkidEmailUser = user;
						}
					}
					
					if (!found) {
						User user = new User();
						user.setMaster("Wizkids");
						user.setMasterId(wuser.getId());
						user.setPrime(true);
						user.setUserId(wuser.getUserName());
						user.setUserType("ACTIVE_DIRECTORY_SCHOOL");
						user.setUuid(UUID.randomUUID().toString());
						
						person.getUsers().add(user);
						
						if (StringUtils.hasText(wuser.getEmail(municipality))) {
							User mailUser = new User();
							mailUser.setMaster("Wizkids");
							mailUser.setMasterId(wuser.getUserName());
							mailUser.setPrime(true);
							mailUser.setUserId(wuser.getEmail(municipality));
							mailUser.setUserType("SCHOOL_EMAIL");
							mailUser.setUuid(UUID.randomUUID().toString());

							person.getUsers().add(mailUser);							
						}
						
						addCount++;
						sofdService.update(person, municipality);
					}
					else {
						boolean changes = false;

						// should never be non-null, but let's be safe
						if (wizkidUser != null) {
							if (!Objects.equals(wizkidUser.getUserId(), wuser.getUserName())) {
								log.info(municipality.getName() + " correcting username on " + wizkidUser.getUserId() + " to " + wuser.getUserName());
								wizkidUser.setUserId(wuser.getUserName());
								changes = true;
							}
						}
						
						if (wizkidEmailUser != null) {
							if (!Objects.equals(wizkidEmailUser.getUserId(), wuser.getEmail(municipality))) {
								log.info(municipality.getName() + " correcting username on " + wizkidEmailUser.getUserId() + " to " + wuser.getEmail(municipality));

								wizkidEmailUser.setUserId(wuser.getEmail(municipality));
								changes = true;
							}
						}
						else {
							User mailUser = new User();
							mailUser.setMaster("Wizkids");
							mailUser.setMasterId(wuser.getUserName());
							mailUser.setPrime(true);
							mailUser.setUserId(wuser.getEmail(municipality));
							mailUser.setUserType("SCHOOL_EMAIL");
							mailUser.setUuid(UUID.randomUUID().toString());

							person.getUsers().add(mailUser);
							
							changes = true;
						}
						
						if (changes) {
							updateCount++;
							sofdService.update(person, municipality);
						}
					}

					break;
				}
			}

			// remove any existing WizkidUsers
			if (!foundWizkidUser) {
				boolean changes = false;

				for (Iterator<User> iterator = person.getUsers().iterator(); iterator.hasNext();) {
					User user = iterator.next();

					if (Objects.equals(user.getUserType(), "ACTIVE_DIRECTORY_SCHOOL") ||
						Objects.equals(user.getUserType(), "SCHOOL_EMAIL")) {
						
						iterator.remove();
						changes = true;
					}
				}

				if (changes) {
					removeCount++;
					sofdService.update(person, municipality);
				}
			}
		}
		
		// creating new persons if needed
		for (WizkidUser wuser : users) {
			boolean found = false;
			
			for (Person person : persons) {
				if (Objects.equals(person.getCpr(), wuser.getCivicNumber())) {
					found = true;
					break;
				}
			}
			
			// create person
			if (!found) {
				Person person = new Person();
				person.setCpr(wuser.getCivicNumber());
				person.setFirstname(wuser.getFirstname());
				person.setSurname(wuser.getLastname());
				person.setMaster("Wizkids");
				person.setUsers(new HashSet<User>());

				User user = new User();
				user.setMaster("Wizkids");
				user.setMasterId(wuser.getId());
				user.setPrime(true);
				user.setUserId(wuser.getUserName());
				user.setUserType("ACTIVE_DIRECTORY_SCHOOL");
				user.setUuid(UUID.randomUUID().toString());
				
				person.getUsers().add(user);
				
				if (StringUtils.hasText(wuser.getEmail(municipality))) {
					User mailUser = new User();
					mailUser.setMaster("Wizkids");
					mailUser.setMasterId(wuser.getUserName());
					mailUser.setPrime(true);
					mailUser.setUserId(wuser.getEmail(municipality));
					mailUser.setUserType("SCHOOL_EMAIL");
					mailUser.setUuid(UUID.randomUUID().toString());

					person.getUsers().add(mailUser);							
				}
				
				createCount++;
				sofdService.create(person, municipality);
			}
		}
		
		log.info(municipality.getName() + " : added " + addCount + ", removed " + removeCount + " users, " + updateCount + " updated and created " + createCount + " persons");
	}
}
