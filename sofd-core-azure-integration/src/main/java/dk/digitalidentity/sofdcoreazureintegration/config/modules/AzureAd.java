package dk.digitalidentity.sofdcoreazureintegration.config.modules;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AzureAd {
	private String baseUrl = "https://graph.microsoft.com/";
	private String loginBaseUrl = "https://login.microsoftonline.com/";
	private String apiVersion = "v1.0";
}