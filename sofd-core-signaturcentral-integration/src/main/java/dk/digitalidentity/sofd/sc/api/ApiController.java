package dk.digitalidentity.sofd.sc.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import dk.digitalidentity.sofd.sc.api.model.MOCES;
import dk.digitalidentity.sofd.sc.dao.model.Municipality;
import dk.digitalidentity.sofd.sc.security.MunicipalityHolder;
import dk.digitalidentity.sofd.sc.service.SyncService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class ApiController {

	@Autowired
	private SyncService syncService;

	@PostMapping("/api/sync")
	public ResponseEntity<?> sync(@RequestBody List<MOCES> moces, @RequestHeader(name = "ClientVersion", required = false, defaultValue = "") String clientVersion) {
		try {
			Municipality municipality = MunicipalityHolder.get();
			municipality.setClientVersion(clientVersion);
			syncService.sync(moces, municipality);
		}
		catch (Exception ex) {
			log.error("Failed to handle sync", ex);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(HttpStatus.OK);
	}
}
