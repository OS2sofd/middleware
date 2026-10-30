package dk.digitalidentity.brugertjek.config.modules;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DevMode {
	private boolean sofdDevMode;
	private boolean rcDevMode;
	private boolean adDevMode;
	private boolean nexusDevMode;
	private boolean os2faktorDevMode;
	private boolean os2syncDevMode;
}
