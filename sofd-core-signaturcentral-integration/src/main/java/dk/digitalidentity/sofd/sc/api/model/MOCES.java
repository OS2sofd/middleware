package dk.digitalidentity.sofd.sc.api.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MOCES {
	private String userId;
	private String subject;
	private transient String rid = null;
	
	public String getRid() {
		if (rid != null) {
			return rid;
		}

		StringBuilder builder = new StringBuilder();
		
		if (subject != null && subject.length() > 0) {
			String tmp = subject.toLowerCase();
			int tmpLen = tmp.length();
			
			int idx = tmp.indexOf("rid:");
			if (idx >= 0) {
				idx += "rid:".length();

				while (idx < tmpLen) {
					if (Character.isDigit(tmp.charAt(idx))) {
						builder.append(tmp.charAt(idx));
						idx++;
					}
					else {
						break;
					}
				}
			}
		}
		
		rid = builder.toString();

		return rid;
	}
}
