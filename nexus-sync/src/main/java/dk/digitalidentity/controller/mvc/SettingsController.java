package dk.digitalidentity.controller.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import dk.digitalidentity.dao.model.Municipality;
import dk.digitalidentity.dao.model.MunicipalitySettings;
import dk.digitalidentity.dao.model.enums.BasedOnRoleOrDefault;
import dk.digitalidentity.dao.model.enums.DataFetchType;
import dk.digitalidentity.dao.model.enums.NationalRole;
import dk.digitalidentity.dao.model.enums.UpdateType;
import dk.digitalidentity.security.RequireAdminAccess;
import dk.digitalidentity.security.SecurityUtil;
import dk.digitalidentity.service.MunicipalityService;
import dk.digitalidentity.service.MunicipalitySettingsService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequireAdminAccess
public class SettingsController {

	@Autowired
	private MunicipalitySettingsService municipalitySettingsService;

	@Autowired
	private MunicipalityService municipalityService;;

	record SettingsForm(UpdateType updateUpn, String missingVendorsMail, String createFailedEmail, boolean disableInitialsUpdate,
					    UpdateType organisationNameUpdateType, String nexusDefaultDepartment, UpdateType nexusUnitUpdateType,
						DataFetchType nexusUnitFetchFrom, String nexusDummyEmailAddress, UpdateType mobileUpdateType,
						UpdateType workPhoneUpdateType, UpdateType addressUpdateType, DataFetchType addressLineFetchFrom,
						String addressLineDefault, DataFetchType postalCodeFetchFrom, String postalCodeDefault,
						DataFetchType cityFetchFrom, String cityDefault, UpdateType professionalJobUpdateType,
						DataFetchType professionalJobFetchFrom, String professionalJobDefault, UpdateType orgsUpdateType,
						UpdateType authorisationCodeUpdateType, BasedOnRoleOrDefault sendToExchangeType, BasedOnRoleOrDefault useDefaultMedcomSenderType,
						BasedOnRoleOrDefault trustType, NationalRole nationalRoleDefaultValue, UpdateType fmkRoleUpdateType,
						DataFetchType fmkRoleFetchFrom, boolean clearCprOnLock, boolean inactivationJobEnabled) {}

	@GetMapping("/settings")
	public String settings(Model model) {
		MunicipalitySettings municipalitySettings = municipalitySettingsService.findByCvr(SecurityUtil.getCvr());
		if (municipalitySettings == null) {
			log.warn("Unknown municipality: " + SecurityUtil.getCvr());
			return "redirect:/error";
		}
		
		Municipality municipality = municipalityService.getByCvr(SecurityUtil.getCvr());
		if (municipality == null) {
			log.warn("Unknown municipality: " + SecurityUtil.getCvr());
			return "redirect:/error";
		}

		model.addAttribute("settings", new SettingsForm(municipalitySettings.getUpdateUpn(), municipalitySettings.getMissingVendorsMail(), municipalitySettings.getCreateFailedEmail(),
				municipalitySettings.isDisableInitialsUpdate(), municipalitySettings.getOrganisationNameUpdateType(), municipalitySettings.getNexusDefaultDepartment(), municipalitySettings.getNexusUnitUpdateType(),
				municipalitySettings.getNexusUnitFetchFrom(), municipalitySettings.getNexusDummyEmailAddress(), municipalitySettings.getMobileUpdateType(),
				municipalitySettings.getWorkPhoneUpdateType(), municipalitySettings.getAddressUpdateType(), municipalitySettings.getAddressLineFetchFrom(),
				municipalitySettings.getAddressLineDefault(), municipalitySettings.getPostalCodeFetchFrom(), municipalitySettings.getPostalCodeDefault(),
				municipalitySettings.getCityFetchFrom(), municipalitySettings.getCityDefault(), municipalitySettings.getProfessionalJobUpdateType(),
				municipalitySettings.getProfessionalJobFetchFrom(), municipalitySettings.getProfessionalJobDefault(), municipalitySettings.getOrgsUpdateType(),
				municipalitySettings.getAuthorisationCodeUpdateType(), municipalitySettings.getSendToExchangeType(), municipalitySettings.getUseDefaultMedcomSenderType(),
				municipalitySettings.getTrustType(), municipalitySettings.getNationalRoleDefaultValue(), municipalitySettings.getFmkRoleUpdateType(),
				municipalitySettings.getFmkRoleFetchFrom(), municipalitySettings.isClearCprOnLock(), municipality.isInactivationJobEnabled()));

		return "settings/settings";
	}

	@PostMapping("/settings")
	public String settings(Model model, @ModelAttribute("settings") SettingsForm settingsForm, RedirectAttributes redirectAttributes) {
		MunicipalitySettings municipalitySettings = municipalitySettingsService.findByCvr(SecurityUtil.getCvr());
		if (municipalitySettings == null) {
			log.warn("Unknown municipality: " + SecurityUtil.getCvr());
			return "redirect:/error";
		}
		
		Municipality municipality = municipalityService.getByCvr(SecurityUtil.getCvr());
		if (municipality == null) {
			log.warn("Unknown municipality: " + SecurityUtil.getCvr());
			return "redirect:/error";
		}

		municipalitySettings.setUpdateUpn(settingsForm.updateUpn());
		municipalitySettings.setMissingVendorsMail(settingsForm.missingVendorsMail());
		municipalitySettings.setCreateFailedEmail(settingsForm.createFailedEmail());
		municipalitySettings.setDisableInitialsUpdate(settingsForm.disableInitialsUpdate());
		municipalitySettings.setOrganisationNameUpdateType(settingsForm.organisationNameUpdateType());
		municipalitySettings.setNexusDefaultDepartment(settingsForm.nexusDefaultDepartment());
		municipalitySettings.setNexusUnitUpdateType(settingsForm.nexusUnitUpdateType());
		municipalitySettings.setNexusUnitFetchFrom(settingsForm.nexusUnitFetchFrom());
		municipalitySettings.setNexusDummyEmailAddress(settingsForm.nexusDummyEmailAddress());
		municipalitySettings.setMobileUpdateType(settingsForm.mobileUpdateType());
		municipalitySettings.setWorkPhoneUpdateType(settingsForm.workPhoneUpdateType());
		municipalitySettings.setAddressUpdateType(settingsForm.addressUpdateType());
		municipalitySettings.setAddressLineFetchFrom(settingsForm.addressLineFetchFrom());
		municipalitySettings.setAddressLineDefault(settingsForm.addressLineDefault());
		municipalitySettings.setPostalCodeFetchFrom(settingsForm.postalCodeFetchFrom());
		municipalitySettings.setPostalCodeDefault(settingsForm.postalCodeDefault());
		municipalitySettings.setCityFetchFrom(settingsForm.cityFetchFrom());
		municipalitySettings.setCityDefault(settingsForm.cityDefault());
		municipalitySettings.setProfessionalJobUpdateType(settingsForm.professionalJobUpdateType());
		municipalitySettings.setProfessionalJobFetchFrom(settingsForm.professionalJobFetchFrom());
		municipalitySettings.setProfessionalJobDefault(settingsForm.professionalJobDefault());
		municipalitySettings.setOrgsUpdateType(settingsForm.orgsUpdateType());
		municipalitySettings.setAuthorisationCodeUpdateType(settingsForm.authorisationCodeUpdateType());
		municipalitySettings.setSendToExchangeType(settingsForm.sendToExchangeType());
		municipalitySettings.setUseDefaultMedcomSenderType(settingsForm.useDefaultMedcomSenderType());
		municipalitySettings.setTrustType(settingsForm.trustType());
		municipalitySettings.setNationalRoleDefaultValue(settingsForm.nationalRoleDefaultValue());
		municipalitySettings.setFmkRoleUpdateType(settingsForm.fmkRoleUpdateType());
		municipalitySettings.setFmkRoleFetchFrom(settingsForm.fmkRoleFetchFrom());
		municipalitySettings.setClearCprOnLock(settingsForm.clearCprOnLock());

		municipalitySettingsService.save(municipalitySettings);
		
		municipality.setInactivationJobEnabled(settingsForm.inactivationJobEnabled());
		municipalityService.save(municipality);

		redirectAttributes.addAttribute("success", "Gemt!");

		return "redirect:/settings";
	}
}
