package dk.sofd.organization.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import dk.sofd.organization.exception.RoleCatalogueNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import dk.sofd.organization.core.CoreService;
import dk.sofd.organization.core.model.ManagerSubstitutePerson;
import dk.sofd.organization.core.model.OrgUnit;
import dk.sofd.organization.core.model.SOFDSubstituteAssignment;
import dk.sofd.organization.dao.model.Municipality;
import dk.sofd.organization.rc.RoleCatalogueService;
import dk.sofd.organization.rc.RoleCatalogueService.ManagerRecord;
import dk.sofd.organization.rc.RoleCatalogueService.SubstituteRecord;
import dk.sofd.organization.service.MunicipalityService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableScheduling
@Component
public class SubstituteSyncTask {
	
	@Autowired
	private RoleCatalogueService roleCatalogueService;

	@Autowired
	private MunicipalityService municipalityService;

	@Autowired
	private CoreService coreService;


	@Scheduled(cron = "${sofd.rollekatalog.substitude_sync_cron:0 3/15 5-21 * * *}")
	public void run() {
		for (Municipality municipality : municipalityService.findAll()) {
			if (!municipality.isSyncSubstitutes()) {
				continue;
			}
			
			try {
				// read all from SOFD
				List<SOFDSubstituteAssignment> managerSubstituteSOFD = getManagerSubstituteAssignmentsFromSOFD(municipality);
				log.info(municipality.getName() + " : SOFD ManagerSubstitute assignments: " + managerSubstituteSOFD.size());

				final List<OrgUnit> allOrgUnits = getAllOrgUnits(municipality);
				log.info(municipality.getName() + " : SOFD OrgUnits: " + allOrgUnits.size());

				// read all from RoleCatalog
				List<ManagerRecord> managerSubstituteRC = getManagerSubstituteAssignmentsFromRoleCatalog(municipality);
				log.info(municipality.getName() + " : RC ManagerSubstitute assignments: " + managerSubstituteRC.size());

				// add
				for (SOFDSubstituteAssignment sofdAssignment : managerSubstituteSOFD) {
					ManagerSubstitutePerson sofdManager = sofdAssignment.getManager();
					ManagerSubstitutePerson sofdSubstitute = sofdAssignment.getSubstitute();

					ManagerRecord rcManager = managerSubstituteRC.stream()
							.filter(ass -> ass.userId().equals(sofdManager.getUserId()))
							.findAny()
							.orElse(null);

					List<OrgUnit> sofdConstraintOUs = sofdAssignment.getConstraintOrgUnits();
					final List<OrgUnit> applicableOrgUnits = findApplicableOrgUnits(sofdConstraintOUs, allOrgUnits, sofdManager);
					if (rcManager != null) {
						// find if this sub is in the RC
						List<SubstituteRecord> rcManagerSubstituteAssignments = rcManager.substitutes().stream()
								.filter(ms -> ms.userId().equals(sofdSubstitute.getUserId()))
								.toList();

						if (!rcManagerSubstituteAssignments.isEmpty()) {
							// found substitute
							List<String> sofdOrgUnits = applicableOrgUnits.stream().map(OrgUnit::getUuid).toList();
							List<String> rcOrgUnits = rcManagerSubstituteAssignments.stream().map(SubstituteRecord::orgUnitUuid).toList();
							
							List<String> toBeAdded = sofdOrgUnits.stream().filter(ou -> !rcOrgUnits.contains(ou)).toList();
							List<String> toBeDeleted = rcOrgUnits.stream().filter(ou -> !sofdOrgUnits.contains(ou)).toList();
							
							for (String orgUnitUuid : toBeAdded) {
								createSubstituteInRC(municipality, sofdManager.getUserId(), sofdSubstitute.getUserId(), orgUnitUuid);
							}
							
							for (String orgUnitUuid : toBeDeleted) {
								deleteSubstituteInRC(municipality, sofdManager.getUserId(), sofdSubstitute.getUserId(), orgUnitUuid);
							}
						}
						else {
							// substitute not found in RC add
							for (OrgUnit orgUnit : applicableOrgUnits) {
								createSubstituteInRC(municipality, sofdManager.getUserId(), sofdSubstitute.getUserId(), orgUnit.getUuid());
							}
						}
					}
					else {
						// manager doesn't exist in RC add the assignment
						for (OrgUnit orgUnit : applicableOrgUnits) {
							createSubstituteInRC(municipality, sofdManager.getUserId(), sofdSubstitute.getUserId(), orgUnit.getUuid());
						}
					}
				}

				// remove
				for (ManagerRecord rcAssignment : managerSubstituteRC) {
					List<SOFDSubstituteAssignment> sofdAssignments = managerSubstituteSOFD.stream()
							.filter(sofd -> sofd.getManager().getUserId().equals(rcAssignment.userId()))
							.toList();
					
					if (!sofdAssignments.isEmpty()) {
						for (SubstituteRecord rcManagerSubstitute : rcAssignment.substitutes()) {
							if (sofdAssignments.stream().noneMatch(
									sofd -> {
										final List<OrgUnit> sofdConstraintOUs = sofd.getConstraintOrgUnits();
										final List<OrgUnit> applicableOrgUnits = findApplicableOrgUnits(sofdConstraintOUs, allOrgUnits, sofd.getManager());
										return sofd.getSubstitute().getUserId().equals(rcManagerSubstitute.userId()) &&
												applicableOrgUnits.stream().anyMatch(
														ou -> ou.getUuid().equals(rcManagerSubstitute.orgUnitUuid()));
									})) {
								
								deleteSubstituteInRC(municipality, rcAssignment.userId(), rcManagerSubstitute.userId(), rcManagerSubstitute.orgUnitUuid());
							}
						}
					}
					else {
						for (SubstituteRecord managerSubstitute : rcAssignment.substitutes()) {
							deleteSubstituteInRC(municipality, rcAssignment.userId(), managerSubstitute.userId(), managerSubstitute.orgUnitUuid());
						}
					}
				}
			}
			catch (Exception ex) {
				log.error("Substitute sync failed (" + municipality.getName() + ")", ex);
			}
		}
	}

	private static List<OrgUnit> findApplicableOrgUnits(final List<OrgUnit> sofdConstraintOUs, final List<OrgUnit> allOrgUnits,
														final ManagerSubstitutePerson sofdManager) {
        return (sofdConstraintOUs != null && !sofdConstraintOUs.isEmpty())
				? sofdConstraintOUs
				: allOrgUnits.stream()
					.filter(ou -> ou.getManagerUuid() != null)
					.filter(ou -> ou.getManagerUuid().equals(sofdManager.getUuid()))
					.toList();
	}

	private void createSubstituteInRC(Municipality municipality, String managerUserId, String substituteUserId, String orgUnitUuid) throws Exception {
		ManagerRecord body = new ManagerRecord(null, managerUserId, new ArrayList<>());
		body.substitutes().add(new SubstituteRecord(null, substituteUserId, orgUnitUuid));

		try {
			log.info(municipality.getName() + " : creating substitute for " + managerUserId + " with substitute " + substituteUserId + " for OU " + orgUnitUuid);
			roleCatalogueService.createSubstitute(municipality, body);
		}
		catch (Exception ex) {
			log.warn(municipality.getName() + " : Failed to create substitute. managerUserId = " + managerUserId + ", substituteUserId=" + substituteUserId + ", orgUnitUuid=" + orgUnitUuid, ex);
		}
	}

	private void deleteSubstituteInRC(Municipality municipality, String managerUserId, String substituteUserId, String orgUnitUuid) throws Exception {
		ManagerRecord body = new ManagerRecord(null, managerUserId, new ArrayList<>());
		body.substitutes().add(new SubstituteRecord(null, substituteUserId, orgUnitUuid));
		
		try {
			log.info(municipality.getName() + " : deleting substitute for " + managerUserId + " with substitute " + substituteUserId + " for OU " + orgUnitUuid);
			roleCatalogueService.deleteSubstitute(municipality, body);
		}
		catch (Exception ex) {
			log.warn(municipality.getName() + " : Failed to delete substitute. managerUserId = " + managerUserId + ", substituteUserId=" + substituteUserId + ", orgUnitUuid=" + orgUnitUuid, ex);
		}
	}

	private List<ManagerRecord> getManagerSubstituteAssignmentsFromRoleCatalog(Municipality municipality) throws Exception {
		List<ManagerRecord> substituteAssignmentsRC = roleCatalogueService.getAllSubstitutes(municipality);
		
		return substituteAssignmentsRC;
	}

	private List<SOFDSubstituteAssignment> getManagerSubstituteAssignmentsFromSOFD(Municipality municipality) throws Exception {
		SOFDSubstituteAssignment[] substituteAssignmentsSOFD = coreService.getSubstituteContextAssignments(municipality);

		return Arrays.asList(substituteAssignmentsSOFD);
	}

	private List<OrgUnit> getAllOrgUnits(Municipality municipality) {
        try {
            return Arrays.asList(coreService.getOrgUnits(municipality));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
