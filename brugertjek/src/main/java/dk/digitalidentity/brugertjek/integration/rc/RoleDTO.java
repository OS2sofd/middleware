package dk.digitalidentity.brugertjek.integration.rc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleDTO {
	private String name;
	private String itSystem;

	public RoleDTO(String role) {
		if (role != null && StringUtils.hasText(role)) {
			Matcher m = Pattern.compile("(.+) \\((.+)\\)").matcher(role);
			if (m.find()) {
				this.name = m.group(1);
				this.itSystem = m.group(2);
			}
		}
	}
}
