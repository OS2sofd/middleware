package dk.sofd.organization.ad.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import dk.sofd.organization.ad.activedirectory.ADUser;
import dk.sofd.organization.ad.dao.model.Municipality;
import dk.sofd.organization.ad.security.MunicipalityHolder;
import dk.sofd.organization.ad.service.SyncService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class ApiController {

	@Autowired
	private SyncService syncService;

	@PostMapping("/api/deltasync")
	public ResponseEntity<?> deltaSync(@RequestBody List<ADUser> users, @RequestHeader(name = "ClientVersion", required = false, defaultValue = "") String clientVersion, @RequestHeader(name = "x-amzn-tls-version", required = false, defaultValue = "") String tlsVersion) {
		try {
			Municipality municipality = MunicipalityHolder.get();
			municipality.setClientVersion(clientVersion);
			municipality.setTlsVersion(tlsVersion);

			syncService.deltaSync(users, municipality);
		}
		catch (Exception ex) {
			log.error("Failed to handle deltaSync", ex);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@PostMapping("/api/fullsync")
	public ResponseEntity<?> fullSync(@RequestBody List<ADUser> users, @RequestHeader(name = "ClientVersion", required = false, defaultValue = "") String clientVersion, @RequestHeader(name = "x-amzn-tls-version", required = false, defaultValue = "") String tlsVersion) {
		try {
			Municipality municipality = MunicipalityHolder.get();
			municipality.setClientVersion(clientVersion);
			municipality.setTlsVersion(tlsVersion);

			syncService.fullSync(users, municipality);
		}
		catch (Exception ex) {
			log.error("Failed to handle fullSync", ex);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(HttpStatus.OK);
	}

}
