package dk.sofd.organization.ad.activedirectory;

import java.util.Map;

import org.springframework.util.StringUtils;

import lombok.Getter;
import lombok.Setter;

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
	private boolean deleted;
	private Boolean disabled;
	private boolean passwordLocked;
	private Map<String, String> localExtensions;
	private byte[] photo;
	private String upn;

	public boolean shouldSynchronizeUser(boolean supportInactiveUsers) {
		if (supportInactiveUsers) {
			return !deleted && hasValidCprAttribute();
		}
		
		return !deleted && !disabled && hasValidCprAttribute();
	}

	public boolean hasValidCprAttribute() {
		// valid Cpr attributes have 10 digits
		return this.getCpr() != null && this.getCpr().matches("^\\d{10}$");
	}

	// ensure empty strings gets treated as NULL for employeeId
	public String getEmployeeId() {
		if ("".equals(this.employeeId)) {
			return null;
		}
		
		return this.employeeId;
	}

	public String getCpr() {
		return cpr != null ? cpr.replaceAll("\\D+", "") : null;
	}

	public String getAffiliation() {
		// ignore leading 0s
		return affiliation != null ? StringUtils.trimLeadingCharacter(affiliation, '0') : null;
	}
	
	public void setChosenName(String chosenName) {
		this.chosenName = chosenName;
	}

	// TODO: do we use this method anywhere in our code anymore? Perhaps implicitly?
	@Override
	public int compareTo(ADUser other) {
		if (this.shouldSynchronizeUser(false) && !other.shouldSynchronizeUser(false)) {
			return -1;
		}
		else if (!this.shouldSynchronizeUser(false) && other.shouldSynchronizeUser(false)) {
			return 1;
		}
		else {
			return this.objectGuid.compareTo(other.objectGuid);
		}
	}
}
