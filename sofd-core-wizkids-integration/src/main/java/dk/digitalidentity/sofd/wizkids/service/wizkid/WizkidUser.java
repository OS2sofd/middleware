package dk.digitalidentity.sofd.wizkids.service.wizkid;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import dk.digitalidentity.sofd.wizkids.dao.model.Municipality;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class WizkidUser {
	private String id;
	private String civicNumber;
	@JsonProperty("isActive")
	private boolean active;
	private String firstname;
	private String middlename;
	private String lastname;
	private String userName;

	public String getName() {
		return firstname + ((middlename != null) ? (" " + middlename + " ") : " ") + lastname;
	}
	
	public String getUserName() {
		if (userName != null && userName.contains("@")) {
			return userName.substring(0, userName.indexOf('@'));
		}
		
		return userName;
	}
	
	public String getEmail(Municipality municipality) {
		if (!StringUtils.hasText(municipality.getMailDomain())) {
			return null;
		}

		String mail = userName;
		if (userName != null && userName.contains("@")) {
			mail = userName.substring(0, userName.indexOf('@'));
		}
		
		return mail + municipality.getMailDomain();
	}
}
