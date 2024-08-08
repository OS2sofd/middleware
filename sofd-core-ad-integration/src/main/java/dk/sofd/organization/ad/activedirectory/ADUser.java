package dk.sofd.organization.ad.activedirectory;

import java.util.Map;

import org.springframework.util.StringUtils;

import dk.sofd.organization.ad.dao.model.Municipality;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class ADUser implements Comparable<ADUser> {
	private String cpr;
	private String affiliation;
	private String title;
	private String chosenName;
	private String firstname;
	private String surname;
	private String email;
	private String mobile;
	private String secretMobile;
	private String phone;
	private String departmentNumber;
	private String faxNumber;
	private String userId;
	private String employeeId;
	private String objectGuid;
	private Long daysToPwdChange; // allow NULL values
	private String accountExpireDate; // map NULL to 9999-12-31
	private String whenCreated;
	private boolean deleted;
	private Boolean disabled;
	private boolean passwordLocked;
	private Map<String, String> localExtensions;
	private byte[] photo;
	private String upn;
	private String mitIDUUID;
	
	private transient boolean cprValidated = false;
	private transient boolean cprValid = false;

	// TODO: why does this exist?
	public boolean shouldSynchronizeUser() {
		return shouldSynchronizeUser(staticMunicipality);
	}

	public boolean shouldSynchronizeUser(Municipality municipality) {
		if (municipality.isSupportInactiveUsers()) {
			return !deleted && hasValidCprAttribute(municipality);
		}
		
		return !deleted && !disabled && hasValidCprAttribute(municipality);
	}

	public boolean hasValidCprAttribute(Municipality municipality) {
		if (cprValidated) {
			return cprValid;
		}

		// clear whitespaces
		if (this.cpr != null) {
			this.cpr = cpr.replaceAll("\\D+", "");
		}

		// decode if needed
		if (municipality.isEncodedCpr()) {
			decodeCpr(municipality);
		}

		// valid cpr attributes have 10 digits
		cprValid = this.cpr != null && this.cpr.matches("^\\d{10}$");
		
		// validation performed - do not do this again
		cprValidated = true;
		
		return cprValid;
	}

    private String decodeCpr(Municipality municipality) {
    	if (this.cpr == null) {
    		return null;
    	}

    	// special rule - if starts with "0000" is is not encoded, but a fictive cpr value
    	if (cpr.startsWith("0000")) {
    		return cpr;
    	}

    	try {
    		long val = Long.parseLong(this.cpr);

            val /= 33;
            val--;

            this.cpr = Long.toString(val);
            if (this.cpr.length() == 9) {
                this.cpr = "0" + this.cpr;
            }
        }
    	catch (Exception ex) {
    		log.error(municipality.getName() + " : Unable to parse cpr: " + this.cpr, ex);
    	}

        return cpr;
    }
    
	// ensure empty strings gets treated as NULL for employeeId
	public String getEmployeeId() {
		if ("".equals(this.employeeId)) {
			return null;
		}
		
		return this.employeeId;
	}

	@Deprecated
	public String getCpr() {
		throw new RuntimeException("Do not use this method - use getCpr(municipality) instead");
	}

	public String getCpr(Municipality municipality) {
		// make sure CPR is valid before continuing
		if (hasValidCprAttribute(municipality)) {
			return cpr.replaceAll("\\D+", "");
		}
		
		return null;
	}

	public String getAffiliation() {
		// ignore leading 0s
		return affiliation != null ? StringUtils.trimLeadingCharacter(affiliation, '0') : null;
	}
	
	public void setChosenName(String chosenName) {
		this.chosenName = chosenName;
	}

	private transient static final Municipality staticMunicipality = new Municipality();

	// TODO: do we use this method anywhere in our code anymore? Perhaps implicitly?
	@Override
	public int compareTo(ADUser other) {
		if (this.shouldSynchronizeUser(staticMunicipality) && !other.shouldSynchronizeUser(staticMunicipality)) {
			return -1;
		}
		else if (!this.shouldSynchronizeUser(staticMunicipality) && other.shouldSynchronizeUser(staticMunicipality)) {
			return 1;
		}
		else {
			return this.objectGuid.compareTo(other.objectGuid);
		}
	}
}
