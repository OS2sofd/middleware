package dk.digitalidentity.service.nexus;

import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import dk.digitalidentity.dao.model.Assignment;
import dk.digitalidentity.dao.model.FailureReason;
import dk.digitalidentity.dao.model.Municipality;
import dk.digitalidentity.dao.model.MunicipalitySettings;
import dk.digitalidentity.dao.model.NexusSofdPositionMapping;
import dk.digitalidentity.dao.model.User;
import dk.digitalidentity.dao.model.enums.BasedOnRoleOrDefault;
import dk.digitalidentity.dao.model.enums.DataFetchType;
import dk.digitalidentity.dao.model.enums.NationalRole;
import dk.digitalidentity.dao.model.enums.UpdateType;
import dk.digitalidentity.service.EmailService;
import dk.digitalidentity.service.MunicipalityService;
import dk.digitalidentity.service.MunicipalitySettingsService;
import dk.digitalidentity.service.NexusSofdPositionMappingService;
import dk.digitalidentity.service.UserService;
import dk.digitalidentity.service.fkorg.FKOrgService;
import dk.digitalidentity.service.nexus.NexusStub.EmployeeWrapper;
import dk.digitalidentity.service.nexus.NexusStub.UpdateResult;
import dk.digitalidentity.service.nexus.model.ActiveDirectoryConfiguration;
import dk.digitalidentity.service.nexus.model.Autosignature;
import dk.digitalidentity.service.nexus.model.Change;
import dk.digitalidentity.service.nexus.model.DefaultOrganizationSupplier;
import dk.digitalidentity.service.nexus.model.Employee;
import dk.digitalidentity.service.nexus.model.EmployeeConfiguration;
import dk.digitalidentity.service.nexus.model.ExchangeConfiguration;
import dk.digitalidentity.service.nexus.model.FMKRoleConfiguration;
import dk.digitalidentity.service.nexus.model.KmdVagtplanConfiguration;
import dk.digitalidentity.service.nexus.model.MunicipalityTrust;
import dk.digitalidentity.service.nexus.model.NationalRoleConfiguration;
import dk.digitalidentity.service.nexus.model.NexusTimeoutException;
import dk.digitalidentity.service.nexus.model.OU;
import dk.digitalidentity.service.nexus.model.PreferencesConfiguration;
import dk.digitalidentity.service.nexus.model.PrimaryOrganization;
import dk.digitalidentity.service.nexus.model.ProfessionalJob;
import dk.digitalidentity.service.rolecatalogue.RoleCatalogueService;
import dk.digitalidentity.service.rolecatalogue.model.ItSystem;
import dk.digitalidentity.service.rolecatalogue.model.ItSystemRole;
import dk.digitalidentity.service.rolecatalogue.model.SystemRole;
import dk.digitalidentity.service.rolecatalogue.model.UserRole;
import dk.digitalidentity.service.rolecatalogue.model.UserRoleAssignment;
import dk.digitalidentity.service.sofd.SofdService;
import dk.digitalidentity.service.sofd.model.Affiliation;
import dk.digitalidentity.service.sofd.model.OrgUnit;
import dk.digitalidentity.service.sofd.model.OrgUnitPost;
import dk.digitalidentity.service.sofd.model.Person;
import dk.digitalidentity.service.sofd.model.Phone;
import dk.digitalidentity.service.sofd.model.SofdUser;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NexusService {
    private static final String USER_ROLE_IDENTIFIER_PREFIX = "NEXUS-ORG-ID-";
    private ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Autowired
    private RoleCatalogueService roleCatalogueService;

    @Autowired
    private UserService userService;

    @Autowired
    private SofdService sofdService;

    @Autowired
    private NexusSofdPositionMappingService nexusSofdPositionMappingService;

    @Autowired
    private FKOrgService fkOrgService;

    @Autowired
    private NexusStub nexusStub;
    
    @Autowired
    private EmailService emailService;

	@Autowired
	private MunicipalitySettingsService municipalitySettingsService;

	@Autowired
	private MunicipalityService municipalityService;

    @Transactional
    public void syncNexusOrgUnitsToUserRoles(Municipality municipality) {
        List<OU> flat = nexusStub.getOusFlat(municipality);
        if (flat == null) {
        	log.warn(municipality.getName() + " : did not find any OUs in Nexus");
        	return;
        }

    	log.info(municipality.getName() + " : found " + flat.size() + " OUs in Nexus");

        ItSystem nexus = new ItSystem();
        nexus.setId(municipality.getRoleCatalogueNexusItSystemId());
        nexus.setName("NexusSync Organisationsroller");
        nexus.setIdentifier("nexusorgroles");
        nexus.setConvertRolesEnabled(true);

        List<ItSystemRole> systemRoles = new ArrayList<>();

        for (OU ou : flat) {
            // we want to skip the root org unit because it is default assigned and can not be deassigned
            if (ou.getId() == 1) {
                continue;
            }

            ItSystemRole systemRole = new ItSystemRole();
            systemRole.setName(ou.getName());
            systemRole.setIdentifier(USER_ROLE_IDENTIFIER_PREFIX + ou.getId());
            systemRoles.add(systemRole);
        }

        nexus.setSystemRoles(systemRoles);

        roleCatalogueService.saveItSystem(nexus, municipality);
    }

    public record AssignmentDTO(long roleId, String roleIdentifier) {};
    
    @Transactional(rollbackFor = Exception.class)
    public void syncNexusOrgUnitAssignments(Municipality municipality) {
        List<UserRole> userRoles = roleCatalogueService.getUserRolesFromItSystem(municipality, "nexusorgroles");
        if (userRoles == null) {
        	return;
        }
        
        // map result to useful datastructures
        Map<String, List<AssignmentDTO>> userNameAssignmentMap = new HashMap<>();
        mapAssignmentsToUserId(userRoles, userNameAssignmentMap);

        log.info(municipality.getName() + " Found " + userNameAssignmentMap.size() + " different users with assignments from OS2rollekatalog");
        if (userNameAssignmentMap.size() == 0) {
        	log.warn(municipality.getName() + " : No assignments - aborting update to be on the safe side");
        	return;
        }

        // read existing knowledge about users (cached data from DB)
        List<User> users = userService.getByMunicipality(municipality);

        for (Map.Entry<String, List<AssignmentDTO>> entry : userNameAssignmentMap.entrySet()) {
            String userId = entry.getKey();
            List<AssignmentDTO> assignments = entry.getValue();

            try {
	            User dbUser = users.stream().filter(u -> u.getUserId().equals(userId)).findFirst().orElse(null);
	            if (dbUser != null) {
	                boolean changes = false;

	                // compute which OUs are assigned
                    Set<Long> shouldBeInOUIds = getDefaultAndAffiliationOuAssignments(municipality, dbUser);
                    Set<Long> rcAssignmentNexusIds = assignments.stream().map(a -> Long.parseLong(a.roleIdentifier().replace(USER_ROLE_IDENTIFIER_PREFIX, ""))).collect(Collectors.toSet());
                    shouldBeInOUIds.addAll(rcAssignmentNexusIds);

                    // flag this user under sync-control
                    if (!dbUser.isControlOrgRoles()) {
		                dbUser.setControlOrgRoles(true);
	                	changes = true;
                    }

	                // check for role changes
                    Set<Long> dbAssignmentNexusIds = dbUser.getAssignments().stream().map(Assignment::getOrgUnitId).collect(Collectors.toSet());

                    long inDbButNotInNew = dbAssignmentNexusIds.stream().filter(a -> !shouldBeInOUIds.contains(a)).count();
                    long inNewButNotInDb = shouldBeInOUIds.stream().filter(a -> !dbAssignmentNexusIds.contains(a)).count();

                    if (inDbButNotInNew > 0 || inNewButNotInDb > 0) {
                        changes = true;
                    }

	                if (changes) {
		                log.info(municipality.getName() + " : changes detected on orgunit roles for " + userId);

	                    updateOrganizations(municipality, shouldBeInOUIds, dbUser);
	                    
	                    userService.save(dbUser);
	                }
	            }
            }
            catch (Exception ex) {
            	if (ex instanceof NexusTimeoutException) {
            		log.warn(municipality.getName() + " : timeout on " + userId, ex);
            	}
            	else {
            		log.error(municipality.getName() + " : Failed to update " + userId, ex);
            	}
            }
        }

        /* TODO: this works very badly with the "isControlOrgRoles" logic for substitutes
        for (User user : users) {
        	try {
	        	// if the syncjob is not in control of this users roles
	        	if (!user.isControlOrgRoles()) {
	        		continue;
	        	}
	        	
	        	if (!userNameAssignmentMap.containsKey(user.getUserId())) {
					log.info(municipality.getName() + " : user no longer has any roles - cleaning up in Nexus : " + user.getUserId());
					
	                updateOrganizations(municipality, getDefaultAndAffiliationOuAssignments(municipality, user), user);
	
	                // and we are no longer in control of this user (until they are assigned roles again)
	                user.setControlOrgRoles(false);
	                userService.save(user);
	            }
        	}
        	catch (Exception ex) {
        		log.error(municipality.getName() + " : Failed to cleanup " + user.getUserId(), ex);
        	}
        }
        */
    }

	@Transactional(rollbackFor = Exception.class)
	public void syncRoleCatalogRoleAssignmentsToNexus(Municipality municipality, MunicipalitySettings municipalitySettings) {
		
		// preload all fmk assignments
		Map<String, Set<SystemRole>> fmkAssignments = new HashMap<>();
		if (Objects.equals(municipalitySettings.getFmkRoleFetchFrom(), DataFetchType.FROM_ROLECATALOG)) {
			log.info(municipality.getName() + " : preloading data from OS2rollekatalog for FMK sync");

			List<UserRole> userRoles = roleCatalogueService.getUserRolesFromItSystem(municipality, "nexusfmk");
			
			if (userRoles != null) {
				for (UserRole userRole : userRoles) {
					for (UserRoleAssignment assignment : userRole.getAssignments()) {
						String userId = assignment.getUserId().toLowerCase();
						
						Set<SystemRole> currentSystemRoles = fmkAssignments.get(userId);
						if (currentSystemRoles == null) {
							currentSystemRoles = new HashSet<>();
							fmkAssignments.put(userId, currentSystemRoles);
						}

						// copy any not already added into currentSystemRoles for that user
						for (SystemRole systemRole : userRole.getSystemRoles()) {
							if (!currentSystemRoles.stream().anyMatch(csr -> Objects.equals(csr.getRoleIdentifier(), systemRole.getRoleIdentifier()))) {
								currentSystemRoles.add(systemRole);
							}
						}
					}
				}
			}
		}

		// preload all national role assignments
		Map<String, Set<String>> nationalAssignments = new HashMap<>();
		if (Objects.equals(municipalitySettings.getNationalRoleDefaultValue(), NationalRole.ROLE_CATALOG)) {
			log.info(municipality.getName() + " : preloading data from OS2rollekatalog for Nationale Roller sync");

			List<UserRole> userRoles = roleCatalogueService.getUserRolesFromItSystem(municipality, "nexusnationale");
			
			if (userRoles != null) {
				for (UserRole userRole : userRoles) {
					Set<String> systemRoleIdentifiers = userRole.getSystemRoles().stream().map(sr -> sr.getRoleIdentifier()).collect(Collectors.toSet());
					
					for (UserRoleAssignment assignment : userRole.getAssignments()) {
						String userId = assignment.getUserId().toLowerCase();
						
						Set<String> currentSystemRoleIdentifiers = nationalAssignments.get(userId);
						if (currentSystemRoleIdentifiers == null) {
							currentSystemRoleIdentifiers = new HashSet<>();
							nationalAssignments.put(userId, currentSystemRoleIdentifiers);
						}
						
						currentSystemRoleIdentifiers.addAll(systemRoleIdentifiers);
					}
				}
			}
		}

		// preload all flag assignments
		Map<String, Set<String>> flagAssignments = new HashMap<>();
		if (Objects.equals(municipalitySettings.getSendToExchangeType(), BasedOnRoleOrDefault.ROLE_CATALOG) ||
			Objects.equals(municipalitySettings.getUseDefaultMedcomSenderType(), BasedOnRoleOrDefault.ROLE_CATALOG) ||
			Objects.equals(municipalitySettings.getTrustType(), BasedOnRoleOrDefault.ROLE_CATALOG)) {

			log.info(municipality.getName() + " : preloading data from OS2rollekatalog for flag sync");

			List<UserRole> userRoles = roleCatalogueService.getUserRolesFromItSystem(municipality, "nexusflag");
			
			if (userRoles != null) {
				for (UserRole userRole : userRoles) {
					Set<String> systemRoleIdentifiers = userRole.getSystemRoles().stream().map(sr -> sr.getRoleIdentifier()).collect(Collectors.toSet());
					
					for (UserRoleAssignment assignment : userRole.getAssignments()) {
						String userId = assignment.getUserId().toLowerCase();
						
						Set<String> currentSystemRoleIdentifiers = flagAssignments.get(userId);
						if (currentSystemRoleIdentifiers == null) {
							currentSystemRoleIdentifiers = new HashSet<>();
							flagAssignments.put(userId, currentSystemRoleIdentifiers);
						}
						
						currentSystemRoleIdentifiers.addAll(systemRoleIdentifiers);
					}
				}
			}
		}

		log.info(municipality.getName() + " : fetching all users from database for sync");

		List<User> users = userService.getByMunicipality(municipality);
		
		log.info(municipality.getName() + " : syncing from OS2rollekatalog for " + users.size() + " users");

		for (User user : users) {
			boolean configChanges = false;
			Set<String> systemRolesForNexusFlags = flagAssignments.get(user.getUserId().toLowerCase());

			try {
				EmployeeConfiguration employeeConfiguration = getEmployeeConfiguration(municipality, user);
				if (employeeConfiguration == null) {
					log.error(municipality.getName() + " : Could not fetch employee configuration for user with userId " + user.getUserId() + ". Skipping this user");
					continue;
				}
	
				// fmk role
				String newFMKRole = null;
				if (Objects.equals(municipalitySettings.getFmkRoleFetchFrom(), DataFetchType.FROM_ROLECATALOG)) {
					newFMKRole = getFMKRole(fmkAssignments.get(user.getUserId().toLowerCase()));
					boolean updatedFMK = setFMKRole(user.getUserId(), municipality, user.getNexusId(), newFMKRole, true, municipalitySettings);
					if (updatedFMK) {
						log.info(municipality.getName() + " : changes detected on FMK role for " + user.getUserId());
					}
				}
	
				// national role
				if (Objects.equals(municipalitySettings.getNationalRoleDefaultValue(), NationalRole.ROLE_CATALOG)) {
					Set<String> systemRolesForNationalRoles = nationalAssignments.get(user.getUserId().toLowerCase());
					String newNationalRole = getNationalRole(systemRolesForNationalRoles);
					if (employeeConfiguration.getNationalRoleConfiguration() == null) {
						employeeConfiguration.setNationalRoleConfiguration(new NationalRoleConfiguration());
					}
	
					boolean updatedNationalRole = setNationalRole(municipality, employeeConfiguration, true, newNationalRole);
					if (updatedNationalRole) {
						log.info(municipality.getName() + " : changes detected on national role for " + user.getUserId());
						configChanges = true;
					}
				}
	
				// flags: exchange
				if (Objects.equals(municipalitySettings.getSendToExchangeType(), BasedOnRoleOrDefault.ROLE_CATALOG)) {
					boolean sendToExchange = hasNexusFlagRole(systemRolesForNexusFlags, "nexus-exchange");
					if (employeeConfiguration.getExchangeConfiguration() == null) {
						employeeConfiguration.setExchangeConfiguration(new ExchangeConfiguration());
					}
	
					if (!Objects.equals(sendToExchange, employeeConfiguration.getExchangeConfiguration().isSendToExchange())) {
						employeeConfiguration.getExchangeConfiguration().setSendToExchange(sendToExchange);
						log.info(municipality.getName() + " : changes detected on send to exchange for " + user.getUserId());
						configChanges = true;
					}
				}
	
				// flags: medcom
				if (Objects.equals(municipalitySettings.getUseDefaultMedcomSenderType(), BasedOnRoleOrDefault.ROLE_CATALOG)) {
					boolean medcomDefaultReply = hasNexusFlagRole(systemRolesForNexusFlags, "nexus-default-medcom");
					if (!Objects.equals(medcomDefaultReply, employeeConfiguration.isReplyToDefaultMedcomSenderOrganization())) {
						employeeConfiguration.setReplyToDefaultMedcomSenderOrganization(medcomDefaultReply);
						log.info(municipality.getName() + " : changes detected on medcom default reply for " + user.getUserId());
						configChanges = true;
					}
				}
	
				// flags: trust
				if (Objects.equals(municipalitySettings.getTrustType(), BasedOnRoleOrDefault.ROLE_CATALOG)) {
					boolean trust = hasNexusFlagRole(systemRolesForNexusFlags, "nexus-trust");
					boolean updatedTrust = setTrust(user.getUserId(), municipality, user.getNexusId(), true, municipalitySettings, newFMKRole, trust);
	
					if (updatedTrust) {
						log.info(municipality.getName() + " : changes detected on trust for " + user.getUserId());
					}
				}
	
				if (configChanges) {
					if (nexusStub.updateEmployeeConfiguration(employeeConfiguration, municipality) == UpdateResult.OK) {
			        	user.setLastEmployeeUpdate(LocalDateTime.now());
			        	userService.save(user);
					}
				}
			}
			catch (NexusTimeoutException ex) {
				log.warn(municipality.getName() + " : Timeout on syncRoleCatalogRoleAssignmentsToNexus " + user.getUserId(), ex);
			}
		}
		
		log.info(municipality.getName() + " : syncing from OS2rollekatalog completed");
	}

	private EmployeeConfiguration getEmployeeConfiguration(Municipality municipality, User user) {
		EmployeeConfiguration employeeConfiguration = null;
		try {
			employeeConfiguration = nexusStub.getEmployeeConfiguration(user.getNexusId(), municipality);
		}
		catch (JsonProcessingException e) {
			return null;
		}

		return employeeConfiguration;
	}

	private final String substituteRegex = "^vik\\d{4}$";
    private boolean isSubstituteUserId(String userId) {
    	if (userId == null) {
    		return false;
    	}

    	return userId.matches(substituteRegex);
	}

	private Set<Long> getDefaultAndAffiliationOuAssignments(Municipality municipality, User dbUser) {
        Set<Long> shouldBeInOUIds = new HashSet<>();
        shouldBeInOUIds.add(municipality.getDefaultOu());
        
        // add all OUs from affiliations
        if (StringUtils.hasLength(dbUser.getOusFromAffiliations())) {
        	for (String tok : dbUser.getOusFromAffiliations().split(",")) {
        		try {
        			Long val = Long.parseLong(tok);
        			if (val != null) {
        				shouldBeInOUIds.add(val);
        			}
        		}
        		catch (Exception ex) {
        			log.warn("Unable to parse '" + tok + "' not a valid Long: " + ex.getMessage());
        		}
        	}
        }

        return shouldBeInOUIds;
	}

	public enum CreateEmployeeStatus { CREATED, CREATED_WITHOUT_CPR, EXISTED_TOOK_CONTROL, EXISTED_FAILED, TEMPORARY_ERROR, PERMANENT_ERROR, BAD_DATA }
    public record CreateEmployeeWrapper(CreateEmployeeStatus status, long employeeId) { };
    
	public CreateEmployeeWrapper createEmployee(Person person, SofdUser user, String kombitUuid, Municipality municipality, List<OrgUnit> ous, List<ProfessionalJob> professionalJobs, List<OU> flatNexusOUs, List<DefaultOrganizationSupplier> defaultOrganizationSuppliers, MunicipalitySettings municipalitySettings) throws JsonProcessingException {
    	log.info(municipality.getName() + " : Creating employee " + user.getUserId() + " / " + kombitUuid);

        Affiliation primeAffiliation = person.getAffiliations().stream().filter(a -> a.isPrime()).findFirst().orElse(null);
        if (primeAffiliation == null) {
            log.warn(municipality.getName() + " : PrimeAffiliation is null for " + user.getUserId() + " / " + kombitUuid);
            return new CreateEmployeeWrapper(CreateEmployeeStatus.TEMPORARY_ERROR, 0);
        }

        OrgUnit primeAffiliationOrgUnit = ous.stream().filter(o -> o.getUuid().equals(primeAffiliation.getCalculatedOrgUnitUuid())).findFirst().orElse(null);
        if (primeAffiliationOrgUnit == null) {
            log.warn(municipality.getName() + " : PrimeAffiliationOrgUnit is null for " + user.getUserId() + " / " + kombitUuid);
            return new CreateEmployeeWrapper(CreateEmployeeStatus.TEMPORARY_ERROR, 0);
        }

        // start by checking manually if the user already exists (getEmployeePrototype does not check for this... sigh)
        Employee existingEmployee = nexusStub.getEmployeeByUuid(municipality, kombitUuid, user.getUserId());
        if (existingEmployee != null) {
        	log.info(municipality.getName() + " : took control of existing user in Nexus " + user.getUserId() + " / " + existingEmployee.getId());
			return new CreateEmployeeWrapper(CreateEmployeeStatus.EXISTED_TOOK_CONTROL, existingEmployee.getId());
        }

        EmployeeWrapper prototypeWrapper = nexusStub.getEmployeePrototype(municipality, kombitUuid);
        switch (prototypeWrapper.status()) {
	        case FAILED:
	        	// failure, try again later
	        	return new CreateEmployeeWrapper(CreateEmployeeStatus.TEMPORARY_ERROR, 0);
	        case TECHNICAL_ERROR:
	        	return new CreateEmployeeWrapper(CreateEmployeeStatus.PERMANENT_ERROR, 0);
	        case OTHER_WITH_SAME_CPR_EXISTS:
	        	if (prototypeWrapper.existingEmployeeId() > 0) {

	        		// if the existing user has the same UUID, just perform an update and then return the ID as a successful create ;)
	        		EmployeeConfiguration employeeConfiguration = nexusStub.getEmployeeConfiguration(prototypeWrapper.existingEmployeeId(), municipality);
	        		if (employeeConfiguration != null && Objects.equals(kombitUuid, employeeConfiguration.getStsSn())) {
	        			return new CreateEmployeeWrapper(CreateEmployeeStatus.EXISTED_TOOK_CONTROL, prototypeWrapper.existingEmployeeId());
	        		}
	        		
		        	log.warn(municipality.getName() + " : Unable to create employee (another has same CPR but different UUID = " + employeeConfiguration.getStsSn() + ") than " + user.getUserId() + " / " + kombitUuid);
	        	}
	        	else {
		        	log.warn(municipality.getName() + " : Unable to create employee (data conflict - perhaps duplicate UnikID) for " + user.getUserId() + " / " + kombitUuid);
	        	}
	        	
	        	return new CreateEmployeeWrapper(CreateEmployeeStatus.EXISTED_FAILED, 0);
	        case OK:
	        	break;
        }

        Employee prototype = prototypeWrapper.employee();
        copyFields(prototype, person, user, municipality, primeAffiliation, primeAffiliationOrgUnit, municipalitySettings);

        String href = prototype.get_links().getCreate().getHref();
        int index = href.indexOf("/v2/");
        String endpoint = href.substring(index + 3);
        var url = municipality.getNexusBaseUrl() + endpoint;
        
        String response = nexusStub.createEmployeeFromStsPrototype(municipality, url, prototype, person, kombitUuid);
        if (response == null) {
        	// failure during create, try again later
        	return new CreateEmployeeWrapper(CreateEmployeeStatus.TEMPORARY_ERROR, 0);
        }
        else if (response.length() == 1) {
        	if ("0".equals(response)) {
        		return new CreateEmployeeWrapper(CreateEmployeeStatus.EXISTED_FAILED, 0);
        	}
        	else if ("1".equals(response)) {
        		return new CreateEmployeeWrapper(CreateEmployeeStatus.BAD_DATA, 0);
        	}
        	else {
        		log.error("Unknown response value: " + response);
        		return new CreateEmployeeWrapper(CreateEmployeeStatus.PERMANENT_ERROR, 0);
        	}
        }

        var employee = mapper.readValue(response, Employee.class);
        if (employee == null) {
            log.error(municipality.getName() + " : Failed to parse created nexus employee " + user.getUserId() + " / " + kombitUuid + " - response payload: " + response);
            return new CreateEmployeeWrapper(CreateEmployeeStatus.TEMPORARY_ERROR, 0);
        }

        // handle preferences
        PreferencesConfiguration preferencesConfiguration = nexusStub.getPreferencesConfiguration(employee.getId(), municipality);
        if (preferencesConfiguration == null) {
            log.warn(municipality.getName() + " : Could not fetch preferences after creating Nexus employee for person with uuid " + kombitUuid);
        }
        else {
            preferencesConfiguration.getCriteria().setCitizenImageVisible(true);
            preferencesConfiguration.getCriteria().setDefaultPatientPage("CITIZEN_DASHBOARD");
            preferencesConfiguration.getCriteria().setDefaultStartPage("CROSS_CITIZEN_DASHBOARD");
            preferencesConfiguration.setDefaultPreference(true);

            nexusStub.createPreferencesConfiguration(employee.getId(), preferencesConfiguration, municipality);
        }

        // handle employeeConfiguration
        EmployeeConfiguration employeeConfiguration = nexusStub.getEmployeeConfiguration(employee.getId(), municipality);
        if (employeeConfiguration == null) {
            log.warn(municipality.getName() + " : Could not fetch employee configuration after creating Nexus employee for person with uuid " + kombitUuid);
        }
        else {
			employeeConfiguration.setPrimaryIdentifier(user.getUserId());
        	
        	// TODO: wait for Nexus change to support CPR on _ALL_ accounts
        	if (user.isPrime()) {
        		employeeConfiguration.setCpr(person.getCpr());
        	}

            handlePrimaryOrgAndMedcomAndDefaultOrgSupplier(municipality, primeAffiliationOrgUnit, employee, employeeConfiguration, defaultOrganizationSuppliers, null);

            // make sure UPN is set during creation (if one exists) - not sure why it must be stored on both objects, seems a bit silly
            if (employee.getActiveDirectoryConfiguration() != null) {
	            if (employee.getActiveDirectoryConfiguration() == null) {
	            	employeeConfiguration.setActiveDirectoryConfiguration(new ActiveDirectoryConfiguration());
	            }
	            employeeConfiguration.getActiveDirectoryConfiguration().setUpn(employee.getActiveDirectoryConfiguration().getUpn());
            }

            // KMD vagtplan - only if cpr has been set (otherwise nexus will reject the change)
            if (StringUtils.hasLength(employeeConfiguration.getCpr())) {
                KmdVagtplanConfiguration kmdVagtplanConfiguration = new KmdVagtplanConfiguration();
                kmdVagtplanConfiguration.setCprExtra(new long[]{municipalitySettings.getKmdVagtplanConfiguration()});
                employeeConfiguration.setKmdVagtplanConfiguration(kmdVagtplanConfiguration);
            }

            // autorisationskode
            if (person.getAuthorizationCode() != null) {
                employeeConfiguration.getAuthorizationCodeConfiguration().setAuthorizationCode(person.getAuthorizationCode());
            }

            // position mapping
            NexusSofdPositionMapping positionMapping = nexusSofdPositionMappingService.getForMunicipalityAndSofdPosition(municipality, primeAffiliation.getPositionName());

            // set professionalJob
            setProfessionalJobOnConfiguration(person, municipality, professionalJobs, primeAffiliation, employeeConfiguration, positionMapping, municipalitySettings, user);

            // FMK role
			String fmkRole = null;
            if (!Objects.equals(municipalitySettings.getFmkRoleFetchFrom(), DataFetchType.FROM_ROLECATALOG) && positionMapping != null && positionMapping.getNexusFmkRole() != null) {
                setFMKRole(user.getUserId(), municipality, employee.getId(), positionMapping.getNexusFmkRole(), false, municipalitySettings);
				fmkRole = positionMapping.getNexusFmkRole();
            }

			// trust
			if (Objects.equals(municipalitySettings.getTrustType(), BasedOnRoleOrDefault.TRUE)) {
				setTrust(user.getUserId(), municipality, employee.getId(), false, municipalitySettings, fmkRole, true);
			}
			else if (Objects.equals(municipalitySettings.getTrustType(), BasedOnRoleOrDefault.FALSE)) {
				setTrust(user.getUserId(), municipality, employee.getId(), false, municipalitySettings, fmkRole, false);
			}

            // check the checkbox for reply to default medcom sender organisation if configured as such
			if (municipalitySettings.getUseDefaultMedcomSenderType() != null && !Objects.equals(municipalitySettings.getUseDefaultMedcomSenderType(), BasedOnRoleOrDefault.NONE) && !Objects.equals(municipalitySettings.getUseDefaultMedcomSenderType(), BasedOnRoleOrDefault.ROLE_CATALOG)) {
				if (municipalitySettings.getUseDefaultMedcomSenderType().equals(BasedOnRoleOrDefault.TRUE)) {
					employeeConfiguration.setReplyToDefaultMedcomSenderOrganization(true);
				}
				else if (municipalitySettings.getUseDefaultMedcomSenderType().equals(BasedOnRoleOrDefault.FALSE)) {
					employeeConfiguration.setReplyToDefaultMedcomSenderOrganization(false);
				}
			}

            // check the checkbox for sendToExchange if configured as such
			if (municipalitySettings.getSendToExchangeType() != null && !Objects.equals(municipalitySettings.getSendToExchangeType(), BasedOnRoleOrDefault.NONE) && !Objects.equals(municipalitySettings.getSendToExchangeType(), BasedOnRoleOrDefault.ROLE_CATALOG)) {
				if (employeeConfiguration.getExchangeConfiguration() == null) {
					employeeConfiguration.setExchangeConfiguration(new ExchangeConfiguration());
				}

				if (municipalitySettings.getSendToExchangeType().equals(BasedOnRoleOrDefault.TRUE)) {
					employeeConfiguration.getExchangeConfiguration().setSendToExchange(true);
				}
				else if (municipalitySettings.getSendToExchangeType().equals(BasedOnRoleOrDefault.FALSE)) {
					employeeConfiguration.getExchangeConfiguration().setSendToExchange(false);
				}
			}


			if (!Objects.equals(municipalitySettings.getNationalRoleDefaultValue(), NationalRole.ROLE_CATALOG) && !Objects.equals(municipalitySettings.getNationalRoleDefaultValue(), NationalRole.NONE)) {
				setNationalRole(municipality, employeeConfiguration, false, municipalitySettings.getNationalRoleDefaultValue().toString());
			}

			if (nexusStub.updateEmployeeConfiguration(employeeConfiguration, municipality) != UpdateResult.OK) {
                log.warn(municipality.getName() + " : Could not update employee configuration after creating Nexus employee for sofd person with uuid " + kombitUuid + " and user with userId " + user.getUserId());
                
                return new CreateEmployeeWrapper(CreateEmployeeStatus.CREATED_WITHOUT_CPR, employee.getId());
            }
        }

        log.info(municipality.getName() + " : Created employee " + user.getUserId() + " / " + kombitUuid);

        return new CreateEmployeeWrapper(CreateEmployeeStatus.CREATED, employee.getId());
    }

	private boolean setNationalRole(Municipality municipality, EmployeeConfiguration employeeConfiguration, boolean checkForUpdate, String nationalRole) {
		if (StringUtils.hasLength(nationalRole)) {
			if (employeeConfiguration.getNationalRoleConfiguration() == null) {
				employeeConfiguration.setNationalRoleConfiguration(new NationalRoleConfiguration());
			}

			boolean update = true;
			if (checkForUpdate && Objects.equals(employeeConfiguration.getNationalRoleConfiguration().getNationalRole(), nationalRole)) {
				update = false;
			}

			if (update) {
				switch (nationalRole) {
					case "SUND_ASSIST_R1":
						employeeConfiguration.getNationalRoleConfiguration().setNationalRole("SUND_ASSIST_R1");
						return true;
					case "SUND_ASSIST_R2":
						employeeConfiguration.getNationalRoleConfiguration().setNationalRole("SUND_ASSIST_R2");
						return true;
					default:
						log.error(municipality.getName() + " : unknown default role: " + nationalRole);
						return false;
				}
			}
		}

		return false;
	}

	private String getNationalRole(Set<String> roles) {
		if (roles == null || roles.isEmpty()) {
			return null;
		}
		else {
			return roles.iterator().next();
		}
	}
	
	private String getFMKRole(Set<SystemRole> roles) {
		SystemRole candidate = null;

		if (roles != null && !roles.isEmpty()) {
			for (SystemRole role : roles) {
				if (candidate == null || role.getWeight() > candidate.getWeight()) {
					candidate = role;
				}
			}
		}
		
		return (candidate != null) ? candidate.getRoleIdentifier() : null;
	}

	private boolean hasNexusFlagRole(Set<String> systemRolesForNexusFlags, String identifier) {
		if (systemRolesForNexusFlags == null) {
			return false;
		}
		else {
			return systemRolesForNexusFlags.contains(identifier);
		}
	}

	public boolean inactivateEmployee(User user, Municipality municipality) throws JsonProcessingException {
        Employee employee = nexusStub.getFullEmployee(user.getNexusId(), municipality);
        if (employee == null) {
            log.warn(municipality.getName() + " : Failed to inactivate Nexus employee. Unable to find Employee for user with userId " + user.getUserId() + " / " + user.getNexusId());
            return true;
        }

        if (!employee.isActive()) {
            log.warn(municipality.getName() + " : Failed to inactivate Nexus employee. Employee already inactive for user with userId " + user.getUserId());
            return true;
        }
        
        employee.setActive(false);
        if (!nexusStub.updateEmployee(employee, municipality)) {
            log.error(municipality.getName() + " : Failed to inactivate Nexus employee. Could not update employee. User with userId " + user.getUserId());
            return false;        	
        }
        else {
        	user.setLastEmployeeUpdate(LocalDateTime.now());
        	userService.save(user);
        }

        EmployeeConfiguration employeeConfiguration = nexusStub.getEmployeeConfiguration(employee.getId(), municipality);
        if (employeeConfiguration == null) {
            log.error(municipality.getName() + " : Failed to inactivate Nexus employee. Could not fetch employee configuration. User with userId " + user.getUserId());
            return false;
        }

        employeeConfiguration.setActive(false);
		MunicipalitySettings municipalitySettings = municipalitySettingsService.findByCvr(municipality.getCvr());
		if (municipalitySettings != null && municipalitySettings.isClearCprOnLock()) {
			employeeConfiguration.setCpr(null);
		}

        if (nexusStub.updateEmployeeConfiguration(employeeConfiguration, municipality) != UpdateResult.OK) {
            log.error(municipality.getName() + " : Failed to inactivate Nexus employee. Could not update employee configuration. User with userId " + user.getUserId());
            return false;
        }
        else {
        	user.setLastEmployeeUpdate(LocalDateTime.now());
        	userService.save(user);
        }

        log.info(municipality.getName() + " : Inactivated user with userId " + user.getUserId() + " in Nexus");

        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public void createEmployees(Municipality municipality) {
        List<OrgUnit> sofdOUs = null;
        try {
            sofdOUs = sofdService.getOrgUnits(municipality);
        }
        catch (Exception ex) {
            log.error(municipality.getName() + " : failed to fetch orgUnits from SOFD", ex);
            return;
        }

		MunicipalitySettings municipalitySettings = municipalitySettingsService.findByCvr(municipality.getCvr());
		if (municipalitySettings == null) {
			log.error(municipality.getName() + " : failed to find municipality settings. Can't update.");
			return;
		}

        // the users that should be in Nexus according to OS2rollekatalog
        Set<String> userIds = roleCatalogueService.getUserIdsWithRolesFromItSystem(municipality);
        if (userIds == null) {
        	log.warn(municipality.getName() + " : could not find roles in OS2rollekatalog - will not create any employees during this run");
        	return;
        }

        // the users that we have previously created in Nexus
        List<User> users = userService.getByMunicipalityIncludingFailures(municipality);
        Set<String> createdUserIds = users.stream().map(User::getUserId).collect(Collectors.toSet());

        // the users we need to create in Nexus
        List<String> toCreate = userIds.stream().filter(u -> !createdUserIds.contains(u)).collect(Collectors.toList());

        // cap to 200 - we are multi-tenant and we do not want to block other municipalities while running this job,
        // so every run creates 200 maximum
        if (toCreate.size() > 200) {
        	toCreate = toCreate.subList(0, 200);
        }
        
        if (toCreate.size() > 0) {
	        log.info(municipality.getName() + " : Creating " + toCreate.size() + " employees");

	        // fetch data from create jobs from Nexus and SOFD
	        List<DefaultOrganizationSupplier> defaultOrganizationSuppliers = nexusStub.getOrganizationSuppliers(municipality);
	        List<ProfessionalJob> professionalJobs = nexusStub.getProfessionalJobs(municipality);
	        List<OU> flatNexusOUs = nexusStub.getOusFlat(municipality);
	        if (flatNexusOUs == null) {
	        	return;
	        }

	        List<User> createdUsers = new ArrayList<>();

	        boolean failedUsers = false;
	        StringBuilder failedUserEmail = new StringBuilder();
	        failedUserEmail.append("Kære Nexus ansvarlig<br/><br/>Følgende brugere kunne IKKE oprettes i Nexus. Årsagen er ukendt, men det kan være fordi der er en eksisterende bruger i Nexus allerede, som har samme Brugernavn, UPN eller CPR nummer.<br><ul>");

	        int counter = 0;
	        for (String userId : toCreate) {
	        	if (++counter % 100 == 0) {
	        		log.info(municipality.getName() + ": created " + counter + " users of " + toCreate.size());
	        	}

	        	// this would be strange - we get the user from OS2rollekatalog, who in turn got it from SOFD, so if the user does
	        	// not exist in SOFD, something is actually quite wrong - but this is just a santity check anyway :)
	            Person sofdPerson = sofdService.getPersonByUserId(userId, municipality);
	            if (sofdPerson == null) {
	                continue;
	            }

	            // filter users that could be relevant to synchronize
	            List<SofdUser> sofdUsers = sofdPerson.getUsers().stream()
	            		.filter(u -> u.getUserType().equals("ACTIVE_DIRECTORY") && (municipality.isSyncAllUsers() || u.isPrime()))
	            		.collect(Collectors.toList());

	            for (SofdUser sofdUser : sofdUsers) {

	            	// only deal with users that NEEDs to be created in Nexus
		            if (!Objects.equals(sofdUser.getUserId(), userId)) {
		            	continue;
		            }

		            // what UUID are we using?
		            final String kombitUuid = StringUtils.hasLength(sofdUser.getKombitUuid()) ? sofdUser.getKombitUuid() : sofdPerson.getUuid();

		            // we have to wait for the person to be created in FK Organisation, as we cannot create a user in Nexus before
		            // the corresponding data exists in FK Organisation :(
		            if (!fkOrgService.isPersonCreated(municipality, kombitUuid)) {
		                log.info(municipality.getName() + " : Person from SOFD with uuid " + kombitUuid + " is not created in FK Organisation yet. Will skip Nexus creation for now.");
		                continue;
		            }
	
		            // attempt to create the user in Nexus
		            CreateEmployeeWrapper resultWrapper = null;
		            try {
		            	resultWrapper = createEmployee(sofdPerson, sofdUser, kombitUuid, municipality, sofdOUs, professionalJobs, flatNexusOUs, defaultOrganizationSuppliers, municipalitySettings);
		            }
		            catch (Exception ex) {
		            	if (ex instanceof NexusTimeoutException) {
		            		log.warn(municipality.getName() + " : timeout on creating user in Nexus : " + userId + " / " + kombitUuid, ex);
		            	}
		            	else {
			                log.error(municipality.getName() + " : Failed to create user in Nexus : " + userId + " / " + kombitUuid, ex);
		            	}
		            }

		            if (resultWrapper != null) {
		            	switch (resultWrapper.status()) {
		            		case CREATED: {
		            			User created = storeCreatedUser(municipality, sofdPerson, userId, kombitUuid, resultWrapper.employeeId(), sofdOUs, flatNexusOUs);
		            			createdUsers.add(created);
		            			break;
		            		}
		            		case CREATED_WITHOUT_CPR: {
		            			User created = storeCreatedUser(municipality, sofdPerson, userId, kombitUuid, resultWrapper.employeeId(), sofdOUs, flatNexusOUs);
		            			
		            			// weird combo - it failed, but not fully
		            			created.setFailedToCreate(true);
		            			created.setFailureReason(FailureReason.OTHER_USER_WITH_SAME_CPR);
		            			createdUsers.add(created);
		            			break;
		            		}
		            		case EXISTED_TOOK_CONTROL: {
		            			User created = storeCreatedUser(municipality, sofdPerson, userId, kombitUuid, resultWrapper.employeeId(), sofdOUs, flatNexusOUs);
		            			createdUsers.add(created);
	
		            			// if we took control, perform an update to make sure it has all the right data
		            			updateUserInNexus(municipality, municipalitySettings, sofdPerson, created, sofdOUs, null);
		            			
		            			break;
		            		}
		            		case EXISTED_FAILED: {
		            			failedUsers = true;
		            			failedUserEmail.append("<li>" + userId + "</li>");
		    	                User created = storeFailedUser(municipality, sofdPerson, userId, kombitUuid, FailureReason.OTHER_USER_WITH_SAME_CPR);
		    	                createdUsers.add(created);
		    	                break;
		            		}
		            		case BAD_DATA: {
		            			failedUsers = true;
		            			failedUserEmail.append("<li>" + userId + " (dårlige data i FK Organisation)</li>");
		    	                User created = storeFailedUser(municipality, sofdPerson, userId, kombitUuid, FailureReason.BAD_DATA_ON_PERSON);
		    	                createdUsers.add(created);
		    	                break;
		            		}
		            		case PERMANENT_ERROR: {
		            			log.warn(municipality.getName() + " : got a permanent error on creating " + userId);
		    	                User created = storeFailedUser(municipality, sofdPerson, userId, kombitUuid, FailureReason.TECHNICAL_ERROR);
		    	                createdUsers.add(created);
		    	                break;
		            		}
		            		case TEMPORARY_ERROR:
		            			break;
		            	}
		            }
	            }
	        }
	        
	        failedUserEmail.append("</ul>");
	        
	        if (failedUsers) {
	        	String message = failedUserEmail.toString();
	        	
	        	if (StringUtils.hasLength(municipalitySettings.getCreateFailedEmail())) {
	        		try {
	        			emailService.sendMessage(municipalitySettings.getCreateFailedEmail().split(";"), "Nexus brugeroprettelser der er fejlet", message);
	        		}
	        		catch (Exception ex) {
	        			log.warn(municipality.getName() + ": Email afsendelse fejlet", ex);
	        		}
	        	}
	        }
	        
	        userService.saveAll(createdUsers);
        }
    }

    public void inactivateEmployees(Municipality municipality) throws Exception {

		log.info(municipality.getName() + " : running disable users");
		
		// get everyone from Users table (the only one we will actually ever disable in Nexus)
        List<User> users = userService.getByMunicipality(municipality);
        Map<Long, List<User>> userMap = users.stream().filter(u -> u.getNexusId() > 0).collect(Collectors.groupingBy(User::getNexusId));

        // get everyone from OS2sofd (no filtering, as the AD account might even have been deleted)
        List<Person> persons = sofdService.getPersons(municipality);
        Map<String, Person> personMap = persons.stream().collect(Collectors.toMap(Person::getUuid, Function.identity()));

        // fetch all employees from Nexus, to check current status
        Employee[] employees = nexusStub.getEmployees(municipality);
        
        log.info(municipality.getName() + " : Found " + employees.length + " employes in Nexus");
        long activeInNexus = 0, matchingUsers = 0, matchingPersons = 0, matchingActiveUsers = 0;
        for (Employee employee : employees) {

        	// skip Employees that are already inactive
        	if (!employee.isActive()) {
        		continue;
        	}
        	
        	activeInNexus++;
        	
        	// skip if employee does not exist in User table
        	List<User> potentialUsers = userMap.get(employee.getId());
        	if (potentialUsers == null || potentialUsers.size() == 0) {
        		continue;
        	}
        	
        	if (potentialUsers.size() > 1) {
        		log.warn("More than one user in database with nexusId " + employee.getId());
        		continue;
        	}
        	
        	User user = potentialUsers.get(0);
        	
        	matchingUsers++;

        	// while we COULD argue that we should disable Nexus users if the corresponding SOFD person does not exist anymore,
        	// I'd rather keep it safe, and ignore those very rare corner cases
        	Person person = personMap.get(user.getSofdPersonUuid());
        	if (person == null) {
        		log.warn(municipality.getName() + " : could not find a person in OS2sofd with uuid " + user.getSofdPersonUuid() + " which NexusSync claims matches user with id " + user.getId());
        		continue;
        	}
        	
        	matchingPersons++;
        	
        	// compare against OS2sofd and find corresponding user, and check if that user is disabled
        	boolean matchingActiveUser = false;
        	for (SofdUser sofdUser : person.getUsers()) {
        		// ignore non-AD users
        		if (!"ACTIVE_DIRECTORY".equals(sofdUser.getUserType())) {
        			continue;
        		}
        		
        		// should be same userId as stored in Nexus
        		if (!sofdUser.getUserId().equalsIgnoreCase(user.getUserId())) {
        			continue;
        		}
        		
        		// if active, flag the boolean so we do not disable
        		if (sofdUser.getDisabled() ==  null || sofdUser.getDisabled() == false) {
        			matchingActiveUser = true;
        			break;
        		}
        	}
        	
        	matchingActiveUsers++;
        	
        	// okay dokay - disable user
        	if (!matchingActiveUser) {
        		inactivateEmployee(user, municipality);
        	}
        }
        
        log.info(municipality.getName() + " : activeInNexus = " + activeInNexus + ", matchingUsers = " + matchingUsers + ", matchingPersons = " + matchingPersons + ", matchingActiveUsers = " + matchingActiveUsers);
    }

    public void reactivateEmployees(Municipality municipality) throws Exception {
    	// fetch all persons in OS2sofd
        List<Person> personsInSofd = sofdService.getPersons(municipality);
        
        // filter out inactive users, so we only have persons with active users
        personsInSofd = personsInSofd
				.stream()
				.filter(p -> p.getUsers()
						.stream()
						.anyMatch(u -> Objects.equals("ACTIVE_DIRECTORY", u.getUserType()) &&
									   (u.getDisabled() == null || u.getDisabled() == false)))
						.collect(Collectors.toCollection(ArrayList::new));
                
        // find all users that WE have control over in Nexus
        List<User> users = userService.getByMunicipality(municipality);
        Map<Long, List<User>> userMap = users.stream().filter(u -> u.getNexusId() > 0).collect(Collectors.groupingBy(User::getNexusId));
        
        // filter the set of persons, so we only look at those that match users in Nexus
        Set<String> personUuidsFromSofd = users.stream().map(c -> c.getSofdPersonUuid()).collect(Collectors.toSet());
        Map<String, Person> personMap = personsInSofd.stream().filter(p -> personUuidsFromSofd.contains(p.getUuid())).collect(Collectors.toMap(Person::getUuid, Function.identity()));

        // so we now have the set of persons, with active AD users, that should also be active in Nexus
        log.info(municipality.getName() + " : making sure " + personsInSofd.size() + " persons are active in Nexus");

        // if we have no active users left, something is wrong, so abort
        if (personMap.size() == 0) {
        	log.warn(municipality + " : no active users in OS2sofd");
        	return;
        }

        // now fetch all employees in Nexus and filter for inactive employees (potential reactivations)
        Employee[] employees = nexusStub.getEmployees(municipality);
        Set<Employee> inactiveEmployees = Arrays.asList(employees).stream().filter(e -> !e.isActive()).collect(Collectors.toSet());

        for (Employee employee : inactiveEmployees) {
        	List<User> potentialUsers = userMap.get(employee.getId());
        	if (potentialUsers == null || potentialUsers.size() == 0) {
        		continue;
        	}
        	
        	if (potentialUsers.size() > 1) {
        		log.warn("More than one user in database with nexusId " + employee.getId());
        		continue;
        	}
        	
        	User user = potentialUsers.get(0);

        	Person person = personMap.get(user.getSofdPersonUuid());
        	if (person == null) {
        		continue;
        	}
        	
        	boolean match = false;
        	String cpr = null;
        	for (SofdUser sofdUser : person.getUsers()) {
        		// ignore disabled AD users (and users that are not AD users)
        		if (!"ACTIVE_DIRECTORY".equals(sofdUser.getUserType()) || (sofdUser.getDisabled() != null && sofdUser.getDisabled() == true)) {
        			continue;
        		}

        		// do we have an ACTIVE user that matches the userId in Nexus?
        		if (sofdUser.getUserId().equalsIgnoreCase(user.getUserId())) {
        			match = true;
        			
        			// only forward CPR for updates if the account is prime
        			if (sofdUser.isPrime()) {
        				cpr = person.getCpr();
        			}
        			break;
        		}
        	}

        	// we found an active AD user that matches the disabled employee in Nexus, so reactivate
        	if (match) {
        		reactivateEmployee(user, municipality, cpr);
        	}
        }
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void updateEmployees(Municipality municipality) {
		MunicipalitySettings municipalitySettings = municipalitySettingsService.findByCvr(municipality.getCvr());
		if (municipalitySettings == null) {
			log.error(municipality.getName() + " : Not updating employees for " + municipality.getName() + ". Could not find municipalitySettings.");
			return;
		}

        // fetch all persons changed in SOFD since last run
        List<Person> peopleChangedInSofd = sofdService.getChangedPersons(municipality);
        if (peopleChangedInSofd.size() == 0) {
        	return;
        }

        // filter out those that does not exist in Nexus (this is update, not create ;))
        List<User> users = userService.getByMunicipality(municipality);
        Set<String> personUuidsFromSofd = users.stream().map(c -> c.getSofdPersonUuid()).collect(Collectors.toSet());
        peopleChangedInSofd = peopleChangedInSofd.stream().filter(p -> personUuidsFromSofd.contains(p.getUuid())).collect(Collectors.toList());

        log.info(municipality.getName() + " : updating " + peopleChangedInSofd.size() + " persons");

        if (peopleChangedInSofd.size() == 0) {
        	return;
        }

        // fetch data to be used for update
        List<OrgUnit> sofdOUs = null;
        try {
            sofdOUs = sofdService.getOrgUnits(municipality);
        }
        catch (Exception ex) {
            log.error(municipality.getName() + " : failed to fetch orgUnits from SOFD", ex);
            return;
        }

        List<OU> flatNexusOUs = nexusStub.getOusFlat(municipality);
        if (flatNexusOUs == null) {
        	return;
        }

        Map<String, List<String>> missingVendorOrganisations = new HashMap<>();

        for (Person changedPerson : peopleChangedInSofd) {
        	List<User> matchingUsers = users.stream().filter(n -> n.getSofdPersonUuid().equals(changedPerson.getUuid())).collect(Collectors.toList());
        	
        	for (User match : matchingUsers) {
        		// only continue with saving if the update was successful (sometimes we delete the user from the users table on failure
	        	UPDATE_RESULT result = updateUserInNexus(municipality, municipalitySettings, changedPerson, match, sofdOUs, missingVendorOrganisations);
	        	switch (result) {
	        		case DELETED:
	        			continue;
	        		case NOOP:
	        			break;
	        		case UPDATED:
	        			match.setLastEmployeeUpdate(LocalDateTime.now());
	        			userService.save(match);
	        			break;
	        	}
	        	
	        	// figure out if we need to update the ousFromAffiliations field on the cached user object
	        	StringBuilder builder = new StringBuilder();
	            List<OU> assignedOUsFromAffiliation = getOUsFromAffiliations(changedPerson, municipality, sofdOUs, flatNexusOUs);
	            assignedOUsFromAffiliation.stream().map(o -> o.getId()).sorted().forEach(id -> {
		        	if (builder.length() > 0) {
		        		builder.append(",");
		        	}

		        	builder.append(Long.toString(id));
	            });
	
	            String val = builder.toString();
	            if (!Objects.equals(val, match.getOusFromAffiliations())) {
		            match.setOusFromAffiliations(val);
		            userService.save(match);
	            }
        	}
        }
        
        if (missingVendorOrganisations.size() > 0) {
        	StringBuilder builder = new StringBuilder();
        	builder.append("Det var ikke muligt at udfylde leverandør organisation for nedenstående brugere. Der findes ikke nogen leverandørorganisation der matcher den enhed brugerne er knyttet til\n");
    		builder.append("<ul>\n");

        	for (String key : missingVendorOrganisations.keySet()) {
        		builder.append("<li>");
        		builder.append(key);
        		builder.append("<ul>\n");
        		
        		for (String userId : missingVendorOrganisations.get(key)) {
        			builder.append("<li>");
        			builder.append(userId);
        			builder.append("</li>\n");
        		}
        		
        		builder.append("</ul></li>\n");
        	}
        	
        	builder.append("</ul>");
        	
        	String message = builder.toString();
    		log.warn(municipality.getName() + " : " + message);

        	if (StringUtils.hasLength(municipalitySettings.getMissingVendorsMail())) {
        		try {
        			emailService.sendMessage(municipalitySettings.getMissingVendorsMail().split(";"), "Manglende leverandørorganisationer i Nexus", message);
        		}
        		catch (Exception ex) {
        			log.error("Failed to send email to : " + municipalitySettings.getMissingVendorsMail(), ex);
        		}
        	}
        }
    }

    public enum UPDATE_RESULT { UPDATED, NOOP, DELETED }

    private UPDATE_RESULT updateUserInNexus(Municipality municipality, MunicipalitySettings municipalitySettings, Person changedPerson, User match, List<OrgUnit> sofdOUs, Map<String, List<String>> missingVendorOrganisation) {

    	// these are cached on lookup, so it is okay to call these from a loop
        List<DefaultOrganizationSupplier> defaultOrganizationSuppliers = nexusStub.getOrganizationSuppliers(municipality);
        List<ProfessionalJob> professionalJobs = nexusStub.getProfessionalJobs(municipality);
    	
    	return updateUserInNexus(municipality, municipalitySettings, changedPerson, match, sofdOUs, professionalJobs, defaultOrganizationSuppliers, missingVendorOrganisation);
    }
    
    private UPDATE_RESULT updateUserInNexus(Municipality municipality, MunicipalitySettings municipalitySettings, Person changedPerson, User match, List<OrgUnit> sofdOUs, List<ProfessionalJob> professionalJobs, List<DefaultOrganizationSupplier> defaultOrganizationSuppliers, Map<String, List<String>> missingVendorOrganisation) {
    	// only used to indicate if saving the user in the user table is okay - and we COULD potentially delete the user in this flow
    	UPDATE_RESULT canSave = UPDATE_RESULT.NOOP;

        List<SofdUser> sofdUsers = changedPerson.getUsers().stream()
        		.filter(u -> u.getUserType().equals("ACTIVE_DIRECTORY") && (municipality.isSyncAllUsers() || u.isPrime()))
        		.collect(Collectors.toList());

        for (SofdUser sofdUser : sofdUsers) {

        	// only update the relevant user
        	if (!Objects.equals(sofdUser.getUserId(), match.getUserId())) {
        		continue;
        	}

        	// what UUID are we using?
            final String kombitUuid = StringUtils.hasLength(sofdUser.getKombitUuid()) ? sofdUser.getKombitUuid() : changedPerson.getUuid();
                        
	        Affiliation primeAffiliation = changedPerson.getAffiliations().stream().filter(Affiliation::isPrime).findFirst().orElse(null);
	        if (primeAffiliation == null) {
	            log.warn(municipality.getName() + " : Will not update nexus employee for person with uuid " + kombitUuid + ". PrimeAffiliation was null. UserId " + match.getUserId());
	            return canSave;
	        }
	
	        OrgUnit primeAffiliationOrgUnit = sofdOUs.stream().filter(o -> o.getUuid().equals(primeAffiliation.getCalculatedOrgUnitUuid())).findFirst().orElse(null);
	        if (primeAffiliationOrgUnit == null) {
	            log.warn(municipality.getName() + " : Will not update nexus employee for person with uuid " + kombitUuid + ". PrimeAffiliation OrgUnit was null. UserId " + match.getUserId());
	            return canSave;
	        }

	        // ok, we have data, so now lookup in Nexus
	        Employee employee = null;
	        EmployeeConfiguration employeeConfiguration = null;
	        try {
	            employee = nexusStub.getFullEmployee(match.getNexusId(), municipality);
	            if (employee == null) {
	            	log.warn(municipality.getName() + " : Failed to find user in Nexus with userId " + match.getUserId() + " / " + match.getNexusId());
	            	return canSave;
	            }
	
	            employeeConfiguration = nexusStub.getEmployeeConfiguration(employee.getId(), municipality);
	            if (employeeConfiguration == null) {
	                log.warn(municipality.getName() + " : Failed to get employee configuration for userId " + match.getUserId() + " / " + match.getNexusId());
	            }
	        }
	        catch (Exception ex) {
	            log.warn(municipality.getName() + " : Failed to get employee or employee configuration for " + match.getUserId() + ". Will not update employee.", ex);
	            return canSave;
	        }

			// update fields
	        boolean changesConfig = false;
	        boolean changesEmployee = false;
	        
	        if (!Objects.equals(employee.getPrimaryIdentifier(), sofdUser.getUserId())) {
	        	log.info(municipality.getName() + " : updating UnikID for " + employee.getId() + " from " + employee.getPrimaryIdentifier() + " to " + sofdUser.getUserId());
	
	        	changesEmployee = true;
	        	employee.setPrimaryIdentifier(sofdUser.getUserId());
	        }
	
			if (!municipalitySettings.isDisableInitialsUpdate()) {
				String initials = sofdUser.getUserId();
				switch (municipality.getInitialsChoice()) {
					case FIRSTNAME :
						String firstname = changedPerson.getFirstname().split(" ")[0].split("-")[0].replaceAll("[^a-zA-Z]", "");
						initials = firstname;
						break;
					case GENERATE :
						if (StringUtils.hasLength(changedPerson.getFirstname()) && StringUtils.hasLength(changedPerson.getSurname())) {

							String shortFirstname = changedPerson.getFirstname().split(" ")[0].split("-")[0].replaceAll("[^a-zA-Z]", "");
							String shortLastname = changedPerson.getSurname().split(" ")[0].split("-")[0].replaceAll("[^a-zA-Z]", "");

							if (shortFirstname.length() > 0 && shortLastname.length() > 0) {
								initials = shortFirstname + shortLastname.substring(0, 1);
							}
						}
						break;
					case USERID :
						initials = sofdUser.getUserId();
						break;
				}
				
				if (!Objects.equals(employee.getInitials(), initials)) {
					log.info(municipality.getName() + " : updating Initials for " + employee.getId() + " from " + employee.getInitials() + " to " + initials);
		
					changesEmployee = true;
					employee.setInitials(initials);
				}
			}
	
	        if (!Objects.equals(employeeConfiguration.getPrimaryIdentifier(), sofdUser.getUserId())) {
	        	log.info(municipality.getName() + " : updating primaryIdentifier for " + employee.getId() + " from " + employeeConfiguration.getPrimaryIdentifier() + " to " + sofdUser.getUserId());
	        	
	            changesConfig = true;
	        	employeeConfiguration.setPrimaryIdentifier(sofdUser.getUserId());            	
	        }
	        
	        if (Objects.equals(municipalitySettings.getUpdateUpn(), UpdateType.UPDATE)) {
	            if (!Objects.equals(employee.getActiveDirectoryConfiguration().getUpn(), sofdUser.getUpn())) {
	            	log.info(municipality.getName() + " : updating UPN for " + sofdUser.getUserId() + " from " + employee.getActiveDirectoryConfiguration().getUpn() + " to " + sofdUser.getUpn());
	
	                changesEmployee = true;
	                changesConfig = true;
	                employee.getActiveDirectoryConfiguration().setUpn(sofdUser.getUpn());
	                employeeConfiguration.getActiveDirectoryConfiguration().setUpn(sofdUser.getUpn());
	            }
	        }
	
	        // only set, never update (for now, until we have full control over multiple codes in SOFD with a UI-driven dropdown to select primary)
	        if (Objects.equals(municipalitySettings.getAuthorisationCodeUpdateType(), UpdateType.UPDATE)) {
	        	if (StringUtils.hasLength(employeeConfiguration.getAuthorizationCodeConfiguration().getAuthorizationCode())) {
	        		;
	        	}
	        	else if (StringUtils.hasLength(changedPerson.getAuthorizationCode())) {
	            	log.info(municipality.getName() + " : setting AuthorizationCode for " + sofdUser.getUserId() + " to " + changedPerson.getAuthorizationCode());
	
	                changesConfig = true;
	                employeeConfiguration.getAuthorizationCodeConfiguration().setAuthorizationCode(changedPerson.getAuthorizationCode());
	            }
	        }
	
	        if (Objects.equals(municipalitySettings.getNexusUnitUpdateType(), UpdateType.UPDATE)) {
				boolean updatedUnit = false;
				if (Objects.equals(municipalitySettings.getNexusUnitFetchFrom(), DataFetchType.FROM_AD)) {
					if (StringUtils.hasLength(sofdUser.getTitle())) {
						if (!Objects.equals(employee.getUnitName(), sofdUser.getTitle())) {
							log.info(municipality.getName() + " : updating positionName for " + sofdUser.getUserId() + " from " + employee.getUnitName() + " to " + sofdUser.getTitle());

							updatedUnit = true;
							changesEmployee = true;
							employee.setUnitName(sofdUser.getTitle());
						} else {
							updatedUnit = true;
						}
					}
				}

				if (Objects.equals(municipalitySettings.getNexusUnitFetchFrom(), DataFetchType.FROM_SOFD) || !updatedUnit) {
					if (!Objects.equals(employee.getUnitName(), primeAffiliation.getPositionName())) {
						log.info(municipality.getName() + " : updating positionName for " + sofdUser.getUserId() + " from " + employee.getUnitName() + " to " + primeAffiliation.getPositionName());

						changesEmployee = true;
						employee.setUnitName(primeAffiliation.getPositionName());
					}
				}
	        }
	
	        if (Objects.equals(municipalitySettings.getOrganisationNameUpdateType(), UpdateType.UPDATE)) {
	            if (!Objects.equals(employee.getOrganizationName(), primeAffiliationOrgUnit.getName())) {
	            	log.info(municipality.getName() + " : updating organisationName for " + sofdUser.getUserId() + " from " + employee.getOrganizationName() + " to " + primeAffiliationOrgUnit.getName());
	
	                changesEmployee = true;
	                employee.setOrganizationName(primeAffiliationOrgUnit.getName());
	            }
	        }

			// mobile
			if (Objects.equals(municipalitySettings.getMobileUpdateType(), UpdateType.UPDATE)) {
				Phone primePhone = changedPerson.getPhones().stream().filter(p -> p.isPrime() && "VISIBLE".equals(p.getVisibility())).findFirst().orElse(null);

				if (primePhone != null && (!Objects.equals(employee.getMobileTelephone(), primePhone.getPhoneNumber()) || !Objects.equals(employee.getHomeTelephone(), primePhone.getPhoneNumber()))) {
					log.info(municipality.getName() + " : updating mobileTelephone and homeTelephone for " + sofdUser.getUserId() + " from " + employee.getMobileTelephone() + " to " + primePhone.getPhoneNumber());

					changesEmployee = true;
					employee.setMobileTelephone(primePhone.getPhoneNumber());
					employee.setHomeTelephone(primePhone.getPhoneNumber());
				}
			}

			// work phone
			if (Objects.equals(municipalitySettings.getWorkPhoneUpdateType(), UpdateType.UPDATE)) {
				Phone primePhone = changedPerson.getPhones().stream().filter(p -> p.isTypePrime() && p.getPhoneType().equals("LANDLINE")).findFirst().orElse(null);
				if (primePhone != null && !Objects.equals(employee.getWorkTelephone(), primePhone.getPhoneNumber())) {
					log.info(municipality.getName() + " : updating workTelephone and homeTelephone for " + sofdUser.getUserId() + " from " + employee.getWorkTelephone() + " to " + primePhone.getPhoneNumber());

					changesEmployee = true;
					employee.setWorkTelephone(primePhone.getPhoneNumber());
				}
			}

			// address
			if (Objects.equals(municipalitySettings.getAddressUpdateType(), UpdateType.UPDATE)) {
				boolean setPrimaryAddressCountryCode = false;

				// addressLine
				OrgUnitPost primePost = primeAffiliationOrgUnit.getPostAddresses().stream().filter(OrgUnitPost::isPrime).findAny().orElse(null);
				if (primePost != null && municipalitySettings.getAddressLineFetchFrom().equals(DataFetchType.FROM_SOFD)) {
					if (!Objects.equals(primePost.getStreet(), employee.getPrimaryAddress().getAddressLine1())) {
						log.info(municipality.getName() + " : updating addressLine1 for " + sofdUser.getUserId() + " from " + employee.getPrimaryAddress().getAddressLine1() + " to " + primePost.getStreet());

						changesEmployee = true;
						employee.getPrimaryAddress().setAddressLine1(primePost.getStreet());
						setPrimaryAddressCountryCode = true;
					}
				}
				else {
					if (StringUtils.hasLength(municipalitySettings.getAddressLineDefault()) && !Objects.equals(municipalitySettings.getAddressLineDefault(), employee.getPrimaryAddress().getAddressLine1())) {
						log.info(municipality.getName() + " : updating addressLine1 for " + sofdUser.getUserId() + " from " + employee.getPrimaryAddress().getAddressLine1() + " to " + municipalitySettings.getAddressLineDefault());

						changesEmployee = true;
						employee.getPrimaryAddress().setAddressLine1(municipalitySettings.getAddressLineDefault());
						setPrimaryAddressCountryCode = true;
					}
				}

				// postalCode
				if (primePost != null && municipalitySettings.getPostalCodeFetchFrom().equals(DataFetchType.FROM_SOFD)) {
					if (!Objects.equals(primePost.getPostalCode(), employee.getPrimaryAddress().getPostalCode())) {
						log.info(municipality.getName() + " : updating postalCode for " + sofdUser.getUserId() + " from " + employee.getPrimaryAddress().getPostalCode() + " to " + primePost.getPostalCode());

						changesEmployee = true;
						employee.getPrimaryAddress().setPostalCode(primePost.getPostalCode());
						setPrimaryAddressCountryCode = true;
					}
				}
				else {
					if (StringUtils.hasLength(municipalitySettings.getPostalCodeDefault()) && !Objects.equals(municipalitySettings.getPostalCodeDefault(), employee.getPrimaryAddress().getPostalCode())) {
						log.info(municipality.getName() + " : updating postalCode for " + sofdUser.getUserId() + " from " + employee.getPrimaryAddress().getPostalCode() + " to " + municipalitySettings.getPostalCodeDefault());

						changesEmployee = true;
						employee.getPrimaryAddress().setPostalCode(municipalitySettings.getPostalCodeDefault());
						setPrimaryAddressCountryCode = true;
					}
				}

				// city
				if (primePost != null && municipalitySettings.getCityFetchFrom().equals(DataFetchType.FROM_SOFD)) {
					if (!Objects.equals(primePost.getCity(), employee.getPrimaryAddress().getPostalDistrict())) {
						log.info(municipality.getName() + " : updating PostalDistrict for " + sofdUser.getUserId() + " from " + employee.getPrimaryAddress().getPostalDistrict() + " to " + primePost.getCity());

						changesEmployee = true;
						employee.getPrimaryAddress().setPostalDistrict(primePost.getCity());
						setPrimaryAddressCountryCode = true;
					}
				}
				else {
					if (StringUtils.hasLength(municipalitySettings.getCityDefault()) && !Objects.equals(municipalitySettings.getCityDefault(), employee.getPrimaryAddress().getPostalDistrict())) {
						log.info(municipality.getName() + " : updating PostalDistrict for " + sofdUser.getUserId() + " from " + employee.getPrimaryAddress().getPostalDistrict() + " to " + municipalitySettings.getCityDefault());

						changesEmployee = true;
						employee.getPrimaryAddress().setPostalDistrict(municipalitySettings.getCityDefault());
						setPrimaryAddressCountryCode = true;
					}
				}
				
				// quickfix - make sure country is always set
				if (!StringUtils.hasLength(employee.getPrimaryAddress().getCountryCode()) && setPrimaryAddressCountryCode) {
					employee.getPrimaryAddress().setCountryCode("dk");
				}
			}

	        // position mapping
	        NexusSofdPositionMapping positionMapping = nexusSofdPositionMappingService.getForMunicipalityAndSofdPosition(municipality, primeAffiliation.getPositionName());
	
	        if (Objects.equals(municipalitySettings.getProfessionalJobUpdateType(), UpdateType.UPDATE)) {
	            long idBefore = (employeeConfiguration.getProfessionalJob() != null) ? employeeConfiguration.getProfessionalJob().getId() : -1;
	
	            setProfessionalJobOnConfiguration(changedPerson, municipality, professionalJobs, primeAffiliation, employeeConfiguration, positionMapping, municipalitySettings, sofdUser);
	            if (employeeConfiguration.getProfessionalJob() != null && !Objects.equals(idBefore, employeeConfiguration.getProfessionalJob().getId())) {
	            	log.info(municipality.getName() + " : updating professionalJob for " + sofdUser.getUserId());
	                changesConfig = true;
	            }
	        }

			// if fetch from roleCatalog don't update (syncRoleAssignmentsToNexus does that) - else only update if specified in fmkRoleUpdateType
	       	String fmkRole = null;
			if (!Objects.equals(municipalitySettings.getFmkRoleFetchFrom(), DataFetchType.FROM_ROLECATALOG) && Objects.equals(municipalitySettings.getFmkRoleUpdateType(), UpdateType.UPDATE) && positionMapping != null && positionMapping.getNexusFmkRole() != null) {
	            fmkRole = positionMapping.getNexusFmkRole();
				boolean updated = setFMKRole(sofdUser.getUserId(), municipality, employee.getId(), positionMapping.getNexusFmkRole(), true, municipalitySettings);

	            if (updated) {
	            	log.info(municipality.getName() + " : updated fmkRole for " + sofdUser.getUserId());
	            }
	        }

			// trust
			boolean updatedTrust = false;
			if (Objects.equals(municipalitySettings.getTrustType(), BasedOnRoleOrDefault.TRUE)) {
				updatedTrust = setTrust(sofdUser.getUserId(), municipality, employee.getId(), true, municipalitySettings, fmkRole, true);
			}
			else if (Objects.equals(municipalitySettings.getTrustType(), BasedOnRoleOrDefault.FALSE)) {
				updatedTrust = setTrust(sofdUser.getUserId(), municipality, employee.getId(), true, municipalitySettings, fmkRole, false);
			}
			
			if (updatedTrust) {
				log.info(municipality.getName() + " : updated trust for " + sofdUser.getUserId());
			}

			// orgs
	        if (Objects.equals(municipalitySettings.getOrgsUpdateType(), UpdateType.UPDATE)) {
	            boolean updated = handlePrimaryOrgAndMedcomAndDefaultOrgSupplier(municipality, primeAffiliationOrgUnit, employee, employeeConfiguration, defaultOrganizationSuppliers, missingVendorOrganisation);
	
	            if (updated) {
	            	log.info(municipality.getName() + " : updating primaryOrgAndMedcomAndDefaultOrgSupplier for " + sofdUser.getUserId());
	                changesConfig = true;
	            }
	        }

	        if (changesConfig) {
	        	// make sure we flip inactive to active in case there are changes
	        	employeeConfiguration.setActive(true);
	        	
	        	UpdateResult res = nexusStub.updateEmployeeConfiguration(employeeConfiguration, municipality);
	        	switch (res) {
	        		case FAILED_DELETE_FROM_USER_TABLE:
		                log.warn(municipality.getName() + " : Failed to save employee configuration for employee with userId (deleting) " + sofdUser.getUserId() + " / " + match.getUserId());
	        			userService.deleteByMunicipalityAndNexusId(municipality, employeeConfiguration.getId());
	        			
	        			// this is a final state - we always return DELETED if something went wrong here
	        			canSave = UPDATE_RESULT.DELETED;
	        			continue;
		        	case FAILED:
		                log.warn(municipality.getName() + " : Failed to save employee configuration for employee with userId " + sofdUser.getUserId() + " / " + match.getUserId());
		                break;
		        	case OK:
		        		if (canSave != UPDATE_RESULT.DELETED) {
		        			canSave = UPDATE_RESULT.UPDATED;
		        		}
		        		
		                log.info(municipality.getName() + " : Updated nexus employee configuration with userId " + sofdUser.getUserId());
		                break;
	        	}
	        }
	
	        if (changesEmployee) {
	        	// make sure we flip inactive to active in case there are changes
	        	employee.setActive(true);
	
	            boolean success = nexusStub.updateEmployee(employee, municipality);
	
	            if (!success) {
	                log.warn(municipality.getName() + " : Failed to save employee for Nexus employee with userId " + sofdUser.getUserId() + " / " + match.getUserId());
	            }
	            else {
	        		if (canSave != UPDATE_RESULT.DELETED) {
	        			canSave = UPDATE_RESULT.UPDATED;
	        		}

	                log.info(municipality.getName() + " : Updated nexus employee with userId " + sofdUser.getUserId());
	            }
	        }
        }
        
        return canSave;
	}

	@Transactional(rollbackFor = Exception.class)
    public void updateTrustAttestation(Municipality municipality) {
        List<Employee> employeesWithTrust = new ArrayList<>();

        for (User user : userService.getByMunicipality(municipality)) {
            Employee employee = null;
            MunicipalityTrust municipalityTrust = null;

            try {
                employee = nexusStub.getFullEmployee(user.getNexusId(), municipality);
                if (employee != null) {
	                municipalityTrust = nexusStub.getMunicipalityTrust(employee.getId(), municipality);
	
	                if (municipalityTrust == null) {
	                    throw new Exception("Failed to get municipality trust");
	                }
                }
            }
            catch (Exception ex) {
            	// if something bad happens, we need to abort, otherwise we would perform a partial update of OS2rollekatalog, which is bad
                log.error(municipality.getName() + " : Failed to get employee or municipality trust for " + user.getUserId() + " / " + user.getNexusId(), ex);
                return;
            }

            if (municipalityTrust != null && municipalityTrust.isTrusted()) {
                employeesWithTrust.add(employee);
            }
        }

        roleCatalogueService.updateAssignmentsForItSystem(municipality, employeesWithTrust);

		municipality.setInitialTrustSyncDone(true);
		municipalityService.save(municipality);
    }
    
    /// INTERNAL LOGIC ///

    private void updateOrganizations(Municipality municipality, Set<Long> shouldBeInOUIds, User user) {
        List<OU> orgs = nexusStub.getUserOrgUnits(user, municipality);
        if (orgs == null) {
            log.warn(municipality.getName() + " : Failed to get assigned organizations for userId " + user.getUserId() + " in Nexus. Will skip sync of the assignments for this userId");
            return;
        }

        // now compute changes, if any, compared to Nexus
        Change change = new Change();
        Set<Long> added = new HashSet<>();
        Set<Long> removed = new HashSet<>();

        // remove the ous that should not be assigned anymore
        for (OU alreadyIn : orgs) {
            boolean found = false;
            for (Long shouldBeIn : shouldBeInOUIds) {
                if (shouldBeIn == alreadyIn.getId()) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                removed.add(alreadyIn.getId());
            }
        }

        // add the ous that should be assigned
        for (Long shouldBeIn : shouldBeInOUIds) {
            boolean found = false;
            for (OU alreadyIn : orgs) {
                if (shouldBeIn == alreadyIn.getId()) {
                    found = true;
                    break;
                }

            }

            if (!found) {
                added.add(shouldBeIn);
            }
        }

        change.setAdded(new ArrayList<>(added));
        change.setRemoved(new ArrayList<>(removed));

        if (!change.getAdded().isEmpty() || !change.getRemoved().isEmpty()) {
        	log.info(municipality.getName() + " : updating organisation roles for " + user.getUserId() + " - adding ("
        				+ String.join(", ", change.getAdded().stream().map(l -> Long.toString(l)).toList()) + ") and removing ("
        				+ String.join(", ", change.getRemoved().stream().map(l -> Long.toString(l)).toList()) + ")");

            nexusStub.changeAssignedOus(change, user.getNexusId(), municipality);

            user.setLastOrganisationUpdate(LocalDateTime.now());
            userService.save(user);
        }

        // we ended up here for some reason, so even if there are no changes, we still need to ensure that the local DB is updated, so
        // we don't end up here again and again ;)
		if (user.getAssignments() == null) {
			user.setAssignments(new ArrayList<>());
		}

        user.getAssignments().clear();
        if (shouldBeInOUIds.size() > 0) {
            user.getAssignments().addAll(shouldBeInOUIds.stream()
        		.map(ouId -> new Assignment(ouId, user))
        		.collect(Collectors.toList())
            );
        }
    }

    private void mapAssignmentsToUserId(List<UserRole> userRoles, Map<String, List<AssignmentDTO>> userNameAssignmentMap) {
        for (UserRole userRole : userRoles) {
            for (UserRoleAssignment assignment : userRole.getAssignments()) {
            	if (!userNameAssignmentMap.containsKey(assignment.getUserId())) {
            		userNameAssignmentMap.put(assignment.getUserId(), new ArrayList<>());
            	}

            	userNameAssignmentMap.get(assignment.getUserId()).add(new AssignmentDTO(userRole.getRoleId(), userRole.getRoleIdentifier()));
            }
        }
    }

    private List<OU> getOUsFromAffiliations(Person person, Municipality municipality, List<OrgUnit> ous, List<OU> flatNexusOUs) {
        List<OU> ousToBeIn = new ArrayList<>();
        
        for (Affiliation affiliation : person.getAffiliations()) {

        	// skip inactive affiliations
        	if (affiliation.getStopDate() != null) {
        		try {
        			LocalDate stopDate = LocalDate.parse(affiliation.getStopDate());
        			
        			if (stopDate.isBefore(LocalDate.now())) {
        				continue;
        			}
        		}
        		catch (Exception ex) {
        			log.warn(municipality.getName() + " : Failed to parse stopDate for " + person.getUuid() + " '" + affiliation.getStopDate() + "' : " + ex.getMessage());
        		}
        	}

            OrgUnit affiliationOrgUnit = ous.stream().filter(o -> o.getUuid().equals(affiliation.getCalculatedOrgUnitUuid())).findFirst().orElse(null);
            if (affiliationOrgUnit == null) {
                continue;
            }
            
            OU match = flatNexusOUs.stream().filter(o -> o.getName().equalsIgnoreCase(affiliationOrgUnit.getName())).findFirst().orElse(null);
            if (match != null) {
                ousToBeIn.add(match);
            }
        }

        return ousToBeIn;
    }

    private boolean setFMKRole(String userId, Municipality municipality, long employeeId, String newFMKRole, boolean checkForChange, MunicipalitySettings municipalitySettings) {
        boolean update = true;
        if (checkForChange) {
            FMKRoleConfiguration oldFmkRoleConfiguration = null;
            try {
                oldFmkRoleConfiguration = nexusStub.getFMKRole(employeeId, municipality);
            }
            catch (Exception e) {
                log.info(municipality.getName() + " : Can't check for FMK role update, so updates no matter what.");
            }

            if (oldFmkRoleConfiguration != null) {
            	// only perform updates if empty or one of the supported ones (any other values are considered manually setup inside Nexus,
            	// and should never be overwritten
            	boolean canUpdate = false;
            	
            	String oldFmkRole = (oldFmkRoleConfiguration != null) ? oldFmkRoleConfiguration.getFmkRole() : null;
            	if (oldFmkRole == null ||
            		"MUNICIPALITY_WORKER_HANDLING_MEDICINE".equals(oldFmkRole) ||
            		"SOCIAL_SERVICE_WORKER".equals(oldFmkRole) ||
            		"NURSE".equals(oldFmkRole) ||
            		"DOCTOR".equals(oldFmkRole)) {
            		canUpdate = true;
            	}

                if (canUpdate && Objects.equals(newFMKRole, oldFmkRoleConfiguration.getFmkRole())) {
                    update = false;
                }
            }
        }

        if (update) {
            // we can't fetch all possible FMK roles from Nexus, so we assume it's the raw ready to use enum in the db or in the RoleCatalog - e.g. NURSE
            // if not, it fails and no FMK role will be sat
            FMKRoleConfiguration fmkRoleConfiguration = new FMKRoleConfiguration();
            fmkRoleConfiguration.setFmkRole(newFMKRole);
            try {
                fmkRoleConfiguration = nexusStub.updateFMKRole(employeeId, fmkRoleConfiguration, municipality);
                if (fmkRoleConfiguration != null) {
                    return true;
                }
            }
            catch (Exception e) {
                log.debug(municipality.getName() + " : Not assigning FMK role to employee after creating Nexus employee for user with userId " + userId);
			}
        }

        return false;
    }

	private boolean setTrust(String userId, Municipality municipality, long employeeId, boolean checkForChange, MunicipalitySettings municipalitySettings, String fmKRole, boolean trust) {
		boolean update = true;
		if (fmKRole == null) {
			try {
				fmKRole = nexusStub.getFMKRole(employeeId, municipality).getFmkRole();
			}
			catch (Exception ignored) {
				;
			}

			if (fmKRole == null) {
				return false;
			}
		}

		// if user doesn't have FMK role MUNICIPALITY_WORKER_HANDLING_MEDICINE trust should be false
		if (!Objects.equals(fmKRole, "MUNICIPALITY_WORKER_HANDLING_MEDICINE")) {
			trust = false;
		}

		if (checkForChange) {
			try {
				boolean currentTrust = nexusStub.getMunicipalityTrust(employeeId, municipality).isTrusted();
				if (currentTrust == trust) {
					update = false;
				}
			}
			catch (Exception ex) {
				log.info(municipality.getName() + " : Can't fetch current trust - updates no matter what for user: " + userId, ex);
			}
		}

		if (update) {
			nexusStub.setMunicipalityTrust(employeeId, municipality, trust);

			return true;
		}

		return false;
	}

    private boolean handlePrimaryOrgAndMedcomAndDefaultOrgSupplier(Municipality municipality, OrgUnit primeAffiliationOrgUnit, Employee employee, EmployeeConfiguration employeeConfiguration, List<DefaultOrganizationSupplier> defaultOrganizationSuppliers, Map<String, List<String>> missingVendorOrganisations) {
        boolean updated = false;
        List<OU> possiblePrimaryOrganisations = nexusStub.getOusForEmployee(employee.getId(), municipality);
        
        OU primaryOrganisation = possiblePrimaryOrganisations.stream().filter(o -> o.getName().equalsIgnoreCase(primeAffiliationOrgUnit.getName())).findFirst().orElse(null);
        if (primaryOrganisation == null) {
        	// if no existing value is set, flag it for reporting (if an existing value is set, we do not report, to give them the option of setting the value and then ignoring the problem)
        	if (employeeConfiguration.getDefaultOrganizationSupplier() == null) {
	        	// if a map is supplied (used for the nightly batchjob), we supply the relevant information for central logging
	        	if (missingVendorOrganisations != null) {
	        		String key = primeAffiliationOrgUnit.getName();
	
	        		List<String> userIds = missingVendorOrganisations.get(key);
	        		if (userIds == null) {
	        			userIds = new ArrayList<>();
	        			missingVendorOrganisations.put(key, userIds);
	        		}
	        		
	        		userIds.add(employee.getPrimaryIdentifier());
	        	}
	        	else {
	                log.warn(municipality.getName() + " : Failed to set primaryOrganisation, defaultMedcomSenderOrganisationId and defaultOrganisationSupplier for employee " + employee.getId() + " / " + employee.getInitials() + ". The OU with name " + primeAffiliationOrgUnit.getName() + " could not be found");
	        	}
        	}
        }
        else {
            PrimaryOrganization primaryOrganizationObj = new PrimaryOrganization();
            primaryOrganizationObj.setId(primaryOrganisation.getId());

            if (employeeConfiguration.getPrimaryOrganization() == null) {
                employeeConfiguration.setPrimaryOrganization(primaryOrganizationObj);
                updated = true;
            }
            else if (!Objects.equals(employeeConfiguration.getPrimaryOrganization().getId(), primaryOrganizationObj.getId())) {
                employeeConfiguration.setPrimaryOrganization(primaryOrganizationObj);
                updated = true;
            }

            DefaultOrganizationSupplier supplier = defaultOrganizationSuppliers.stream().filter(s -> s.getOrganizationId() == primaryOrganisation.getId()).findAny().orElse(null);
            if (supplier == null) {
            	
            	// if no existing value is set, flag it for reporting (if an existing value is set, we do not report, to give them the option of setting the value and then ignoring the problem)
            	if (employeeConfiguration.getDefaultOrganizationSupplier() == null) {

	            	// if a map is supplied (used for the nightly batchjob), we supply the relevant information for central logging
	            	if (missingVendorOrganisations != null) {
	            		String key = primeAffiliationOrgUnit.getName();
	
	            		List<String> userIds = missingVendorOrganisations.get(key);
	            		if (userIds == null) {
	            			userIds = new ArrayList<>();
	            			missingVendorOrganisations.put(key, userIds);
	            		}
	            		
	            		userIds.add(employee.getPrimaryIdentifier());
	            	}
	            	else {
	            		log.warn(municipality.getName() + " : Failed to set defaultOrganisationSupplier for employee " + employee.getId() + " / " + employee.getInitials() + ". The primaryOrganisation " + primeAffiliationOrgUnit.getId() + " / " + primeAffiliationOrgUnit.getName() + " could not be found in the list of defaultOrganisationSuppliers");
	            	}
            	}
            }
			else if (employeeConfiguration.getDefaultOrganizationSupplier() == null) {
				employeeConfiguration.setDefaultOrganizationSupplier(supplier);
				updated = true;
			}
			else if (!Objects.equals(employeeConfiguration.getDefaultOrganizationSupplier().getOrganizationId(), supplier.getOrganizationId())) {
				employeeConfiguration.setDefaultOrganizationSupplier(supplier);
				updated = true;
			}
        }
        
        // and medcom stuff goes here
        List<OU> possibleMedcomOrganisations = nexusStub.getMedcomOus(municipality);
        OU medcomOrganisation = possibleMedcomOrganisations.stream().filter(o -> o.getName().equalsIgnoreCase(primeAffiliationOrgUnit.getName())).findFirst().orElse(null);

        if (medcomOrganisation != null) {
            if (employeeConfiguration.getDefaultMedcomSenderOrganizationId() == null ||  employeeConfiguration.getDefaultMedcomSenderOrganizationId() == 0) {
                employeeConfiguration.setDefaultMedcomSenderOrganizationId(medcomOrganisation.getId());
                updated = true;
            }
            else if (!Objects.equals(employeeConfiguration.getDefaultMedcomSenderOrganizationId(), medcomOrganisation.getId())) {
                employeeConfiguration.setDefaultMedcomSenderOrganizationId(medcomOrganisation.getId());
                updated = true;
            }
        }

        return updated;
    }

    private void setProfessionalJobOnConfiguration(Person person, Municipality municipality, List<ProfessionalJob> professionalJobs, Affiliation primeAffiliation, EmployeeConfiguration employeeConfiguration, NexusSofdPositionMapping positionMapping, MunicipalitySettings municipalitySettings, SofdUser sofdUser) {
		ProfessionalJob matchProfessionalJob = null;
		if (municipalitySettings.getProfessionalJobFetchFrom().equals(DataFetchType.FROM_AD)) {
			if (sofdUser != null && StringUtils.hasLength(sofdUser.getTitle())) {
				matchProfessionalJob = professionalJobs.stream()
						.filter(p -> p.isActive() && p.getName().equalsIgnoreCase(sofdUser.getTitle()))
						.findAny()
						.orElse(null);
			}
		}

		if (municipalitySettings.getProfessionalJobFetchFrom().equals(DataFetchType.FROM_SOFD) || matchProfessionalJob == null) {
			// see if there is a direct match between the affiliation and any of the professionalJobs
			matchProfessionalJob = professionalJobs.stream()
					.filter(p -> p.isActive() && p.getName().equalsIgnoreCase(primeAffiliation.getPositionName()))
					.findAny()
					.orElse(null);

			// if we have been given a positionMapping, attempt to find a match for that one
			if (matchProfessionalJob == null && positionMapping != null) {
				matchProfessionalJob = professionalJobs.stream()
						.filter(p -> p.isActive() && p.getName().equalsIgnoreCase(positionMapping.getNexusProfessionalJob()))
						.findAny()
						.orElse(null);

				if (matchProfessionalJob == null) {
					log.error(municipality.getName() + " : Failed to set professional job for person " + person.getUuid() + " / " + person.getFirstname() + " " + person.getSurname() + ". The NexusSofdPositionMapping with id " + positionMapping.getId() + " has a nexusProfessionalJob that could not be found. Setting default professional job if possible.");
				}
			}
		}

		// finally, attempt to match the default job if one of the two cases above is null or DataFetchType.DEFAULT_DATA
        if (matchProfessionalJob == null && StringUtils.hasLength(municipalitySettings.getProfessionalJobDefault())) {
			matchProfessionalJob = professionalJobs.stream()
            		.filter(p -> p.isActive() && p.getName().equalsIgnoreCase(municipalitySettings.getProfessionalJobDefault()))
            		.findAny()
            		.orElse(null);
            
            if (matchProfessionalJob == null) {
            	Set<String> positions = professionalJobs.stream().filter(p -> p.isActive()).map(p -> p.getName()).collect(Collectors.toSet());
            	
            	log.error(municipality.getName() + " : Failed to set professional job for person " + person.getUuid() + " / " + person.getFirstname() + " " + person.getSurname() + ". The default professional job could not be found in: " + String.join(",", positions));
            }
        }

        if (matchProfessionalJob != null) {
            employeeConfiguration.setProfessionalJob(matchProfessionalJob);
        }
    }

    private boolean reactivateEmployee(User user, Municipality municipality, String cpr) {
    	Employee employee = nexusStub.getFullEmployee(user.getNexusId(), municipality);
    	if (employee != null) {
    		if (reactivateEmployee(employee, municipality, cpr)) {
    			user.setLastEmployeeUpdate(LocalDateTime.now());
    			user.setLastEmployeeUpdate(LocalDateTime.now());
    			userService.save(user);
    			
    			return true;
    		}

    		return false;
    	}
    	else {
    		log.warn(municipality.getName() + ": Unable to find employee for reactivation " + user.getNexusId());
    	}

    	return false;
	}

    private boolean reactivateEmployee(Employee employee, Municipality municipality, String cpr) {
    	boolean reactivated = false;

    	// clear input CPR if we do not clear on lock (since we then do not set on reactivate)
		MunicipalitySettings municipalitySettings = municipalitySettingsService.findByCvr(municipality.getCvr());
		if (municipalitySettings == null || !municipalitySettings.isClearCprOnLock()) {
			cpr = null;
		}

    	try {
	    	if (!employee.isActive()) {
		    	employee.setActive(true);
	
		        reactivated = nexusStub.updateEmployee(employee, municipality);
	    	}

        	EmployeeConfiguration employeeConfiguration = nexusStub.getEmployeeConfiguration(employee.getId(), municipality);
            if (!employeeConfiguration.isActive()) {
	            employeeConfiguration.setActive(true);
	            boolean twoTries = false;
	            if (cpr != null && employeeConfiguration.getCpr() == null) {
	            	twoTries = true;
	            	employeeConfiguration.setCpr(cpr);
	            }

	            boolean result = (nexusStub.updateEmployeeConfiguration(employeeConfiguration, municipality) == UpdateResult.OK);
	            if (!result && twoTries) {
	            	employeeConfiguration.setCpr(null);
	            	
	            	result = (nexusStub.updateEmployeeConfiguration(employeeConfiguration, municipality) == UpdateResult.OK);
	            }

	            reactivated |= result;
            }
        }
        catch (Exception ex) {
        	log.warn(municipality.getName() + " : Failed to reactivate " + employee.getPrimaryIdentifier(), ex);
            return false;
        }

        if (reactivated) {
        	log.info(municipality.getName() + " : reactivated " + employee.getPrimaryIdentifier());
        }

        return reactivated;
    }

    private String sanatizeName(String name) {
    	return name
    			.replace("&", "og")
    			.replace("/", "");
    }
    
    private void copyFields(Employee to, Person person, SofdUser user, Municipality municipality, Affiliation primeAffiliation, OrgUnit primeAffiliationOrgUnit, MunicipalitySettings municipalitySettings) {
        to.setFirstName(sanatizeName(person.getFirstname()));
        to.setMiddleName(null);
        to.setLastName(sanatizeName(person.getSurname()));
        to.setFullName(sanatizeName(person.getFirstname()) + " " + sanatizeName(person.getSurname()));
        to.setPrimaryIdentifier(user.getUserId());
        
        switch (municipality.getInitialsChoice()) {
            case FIRSTNAME :
                String firstname = person.getFirstname().split(" ")[0].split("-")[0].replaceAll("[^a-zA-Z]", "");
                to.setInitials(firstname);
                break;
            case GENERATE :
                if (StringUtils.hasLength(person.getFirstname()) && StringUtils.hasLength(person.getSurname())) {

                    String shortFirstname = person.getFirstname().split(" ")[0].split("-")[0].replaceAll("[^a-zA-Z]", "");
                    String shortLastname = person.getSurname().split(" ")[0].split("-")[0].replaceAll("[^a-zA-Z]", "");

                    if (shortFirstname.length() > 0 && shortLastname.length() > 0) {
                        to.setInitials(shortFirstname + shortLastname.substring(0, 1));
                    } else {
                        to.setInitials(user.getUserId());
                    }
                }
                else {
                    to.setInitials(user.getUserId());
                }
                break;
            case USERID :
                to.setInitials(user.getUserId());
                break;
        }

        if (!StringUtils.hasLength(to.getPrimaryEmailAddress())) {
            SofdUser primaryExchangeUser = person.getUsers().stream()
            		.filter(u -> u.isPrime() && u.getUserType().equals("EXCHANGE"))
            		.findFirst()
            		.orElse(null);
            
            if (primaryExchangeUser != null) {
                to.setPrimaryEmailAddress(primaryExchangeUser.getUserId());
            }
            else {
                to.setPrimaryEmailAddress(municipalitySettings.getNexusDummyEmailAddress());
            }
        }

        Phone primePhone = person.getPhones().stream().filter(Phone::isPrime).findFirst().orElse(null);
        if (primePhone != null) {
            to.setMobileTelephone(primePhone.getPhoneNumber());
            to.setHomeTelephone(primePhone.getPhoneNumber());
        }

		if (!Objects.equals(municipalitySettings.getWorkPhoneUpdateType(), UpdateType.NO)) {
			Phone primeWorkPhone = person.getPhones().stream().filter(p -> p.isTypePrime() && p.getPhoneType().equals("LANDLINE")).findFirst().orElse(null);
			if (primeWorkPhone != null) {
				to.setWorkTelephone(primeWorkPhone.getPhoneNumber());
			}
		}

        if (user.getUpn() != null && !StringUtils.hasLength(to.getActiveDirectoryConfiguration().getUpn())) {
            ActiveDirectoryConfiguration config = new ActiveDirectoryConfiguration();
            config.setUpn(user.getUpn());
            to.setActiveDirectoryConfiguration(config);
        }

        to.setOrganizationName(primeAffiliationOrgUnit.getName());
        to.setDepartmentName(municipalitySettings.getNexusDefaultDepartment());

		boolean updatedUnit = false;
		if (Objects.equals(municipalitySettings.getNexusUnitFetchFrom(), DataFetchType.FROM_AD)) {
			if (StringUtils.hasLength(user.getTitle())) {
				updatedUnit = true;
				to.setUnitName(user.getTitle());
			}
		}

		if (Objects.equals(municipalitySettings.getNexusUnitFetchFrom(), DataFetchType.FROM_SOFD) || !updatedUnit) {
			to.setUnitName(primeAffiliation.getPositionName());
		}

        List<Autosignature> autosignatures = nexusStub.getAutosignatures(municipality);
        Autosignature standardAutosignature = autosignatures.stream().filter(a -> a.getName().equalsIgnoreCase("Standard")).findAny().orElse(null);
        if (standardAutosignature != null) {
        	to.setAutosignatureId(standardAutosignature.getId());
        }

		OrgUnitPost primePost = primeAffiliationOrgUnit.getPostAddresses().stream().filter(OrgUnitPost::isPrime).findAny().orElse(null);
		
		boolean setPrimaryAddressCountryCode = false;
		if (primePost != null && municipalitySettings.getAddressLineFetchFrom().equals(DataFetchType.FROM_SOFD)) {
			to.getPrimaryAddress().setAddressLine1(primePost.getStreet());
			setPrimaryAddressCountryCode = true;
		}
		else {
			if (municipalitySettings.getAddressLineDefault() != null && !municipalitySettings.getAddressLineDefault().isEmpty()) {
				to.getPrimaryAddress().setAddressLine1(municipalitySettings.getAddressLineDefault());
				setPrimaryAddressCountryCode = true;
			}
		}

		if (primePost != null && municipalitySettings.getPostalCodeFetchFrom().equals(DataFetchType.FROM_SOFD)) {
			to.getPrimaryAddress().setPostalCode(primePost.getPostalCode());
			setPrimaryAddressCountryCode = true;
		}
		else {
			if (municipalitySettings.getPostalCodeDefault() != null && !municipalitySettings.getPostalCodeDefault().isEmpty()) {
				to.getPrimaryAddress().setPostalCode(municipalitySettings.getPostalCodeDefault());
				setPrimaryAddressCountryCode = true;
			}
		}

		if (primePost != null && municipalitySettings.getCityFetchFrom().equals(DataFetchType.FROM_SOFD)) {
			to.getPrimaryAddress().setPostalDistrict(primePost.getCity());
			setPrimaryAddressCountryCode = true;
		}
		else {
			if (municipalitySettings.getCityDefault() != null && !municipalitySettings.getCityDefault().isEmpty()) {
				to.getPrimaryAddress().setPostalDistrict(municipalitySettings.getCityDefault());
				setPrimaryAddressCountryCode = true;
			}
		}

		if (setPrimaryAddressCountryCode) {
			to.getPrimaryAddress().setCountryCode("dk");
		}

        to.setActive(true);
    }

    private User storeFailedUser(Municipality municipality, Person person, String userId, String kombitUuid, FailureReason failureReason) {
        User created = new User();
        created.setMunicipality(municipality);
        created.setNexusId(0);
        created.setSofdPersonUuid(person.getUuid());
        created.setSofdKombitUuid(kombitUuid);
        created.setUserId(userId);
        created.setName(person.getFirstname() + " " + person.getSurname());
        created.setFailedToCreate(true);
      	created.setFailureReason(failureReason);
      	
      	return created;
    }
    
    private User storeCreatedUser(Municipality municipality, Person person, String userId, String kombitUuid, long employeeId, List<OrgUnit> sofdOUs, List<OU> flatNexusOUs) {
    	User created = new User();
        created.setMunicipality(municipality);
        created.setNexusId(employeeId);
        created.setSofdPersonUuid(person.getUuid());
        created.setSofdKombitUuid(kombitUuid);
        created.setUserId(userId);
        created.setName(person.getFirstname() + " " + person.getSurname());
        created.setFailureReason(FailureReason.NONE);
		
		// place employee in affiliation ous
        List<OU> assignedOUsFromAffiliation = getOUsFromAffiliations(person, municipality, sofdOUs, flatNexusOUs);
        StringBuilder builder = new StringBuilder();
        for (OU ou : assignedOUsFromAffiliation) {
        	if (builder.length() > 0) {
        		builder.append(",");
        	}
        	builder.append(Long.toString(ou.getId()));
        }

        created.setOusFromAffiliations(builder.toString());

        if (person.getAffiliations() != null && !person.getAffiliations().isEmpty()) {
            String ouUuid = person.getAffiliations().stream().filter(Affiliation::isPrime).map(Affiliation::getCalculatedOrgUnitUuid).findAny().orElse(null);
            if (ouUuid != null) {
                OrgUnit affiliationOrgUnit = sofdOUs.stream().filter(o -> o.getUuid().equals(ouUuid)).findFirst().orElse(null);
                if (affiliationOrgUnit != null) {
                    created.setOuName(affiliationOrgUnit.getName());
                }
            }
        }
        
        return created;
	}

    // the purpose of this is just to ensure that substitutes actually work, cleanup is KMDs job (and they do that a lot, so let them have it ;))
    @Transactional
	public void fixOrgRoleAssignmentsOnSubstitutes(Municipality municipality) throws URISyntaxException {

		// read ALL users from Nexus
		Employee[] employees = nexusStub.getEmployees(municipality);

		// filter those that are temps (vikXXXX)
		List<Employee> allSubstitutes = Arrays.asList(employees).stream().filter(e -> isSubstituteUserId(e.getPrimaryIdentifier())).collect(Collectors.toList());
		
		// check if they have any roles in Users table, skip if none are present
		List<User> users = userService.getByMunicipality(municipality);

		// update status to active if they are disabled and make sure roles are ADDED if needed
		for (Employee employee : allSubstitutes) {
			try {
				User user = users.stream().filter(u -> Objects.equals(u.getUserId(), employee.getPrimaryIdentifier())).findFirst().orElse(null);
				if (user == null) {
					continue;
				}
				
				// skip users we do not update roles for
				if (!user.isControlOrgRoles()) {
					continue;
				}

				// we assume that the DB is correct (the job runs every 15 minutes, and updates from RC, so it should be
	            Set<Long> dbAssignmentNexusIds = user.getAssignments().stream().map(Assignment::getOrgUnitId).collect(Collectors.toSet());
	            Set<Long> dbOuNexusIds = getDefaultAndAffiliationOuAssignments(municipality, user);
	
	            Set<Long> shouldBeInOus = new HashSet<>();
	            shouldBeInOus.addAll(dbAssignmentNexusIds);
	            shouldBeInOus.addAll(dbOuNexusIds);
	            
	            updateOrganizations(municipality, shouldBeInOus, user);
			}
			catch (NexusTimeoutException ex) {
				log.warn(municipality.getName() + " : Timeout on updating: " + employee.getInitials(), ex);
			}
		}
	}
}
