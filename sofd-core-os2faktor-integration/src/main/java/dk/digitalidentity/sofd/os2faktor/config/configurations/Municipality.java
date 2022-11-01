package dk.digitalidentity.sofd.os2faktor.config.configurations;

import lombok.Getter;
import lombok.Setter;

/**
 * @deprecated
 */
@Deprecated
@Getter
@Setter
public class Municipality {
	private String name;
	private boolean allowNsisForEveryone = false;
	private CoreDataConfiguration os2faktor = new CoreDataConfiguration();
	private SofdCoreConfiguration sofd = new SofdCoreConfiguration();
	private RoleCatalogConfiguration rc = new RoleCatalogConfiguration();
}
