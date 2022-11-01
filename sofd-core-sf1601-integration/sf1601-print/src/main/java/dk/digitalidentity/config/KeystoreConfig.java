package dk.digitalidentity.config;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Base64;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class KeystoreConfig {
	private String location;
	private String password;
	
	private boolean initialized = false;
	private String base64EncodedCertificate;
	
	public String getBase64EncodedCertificate() {
		if (!initialized) {
			if (location != null) {
				try {
					KeyStore ks = KeyStore.getInstance("PKCS12");
					ks.load(new FileInputStream(location), password.toCharArray());
					
					String alias = ks.aliases().nextElement();
					X509Certificate certificate = (X509Certificate) ks.getCertificate(alias);
					
					base64EncodedCertificate = Base64.getEncoder().encodeToString(certificate.getEncoded());
				}
				catch (Exception ex) {
					log.error("Failed to initialize keystore", ex);
				}
			}
			
			initialized = true;
		}
		
		return base64EncodedCertificate;
	}
}
