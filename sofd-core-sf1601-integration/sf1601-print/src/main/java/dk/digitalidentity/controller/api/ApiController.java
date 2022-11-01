package dk.digitalidentity.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import dk.digitalidentity.service.SF1601Service;
import dk.digitalidentity.service.enums.Status;

@RestController
public class ApiController {

	@Autowired
	private SF1601Service ngdpService;

	public record LetterDTO(String cpr, String cvr, String subject, String municipalityName, String content, AttachmentDTO[] attachments) { }
	public record AttachmentDTO(String filename, String content) {}

	@PostMapping(value = "/api/print")
	public ResponseEntity<?> updateIntegration(@RequestBody LetterDTO letterDTO) {
		Status status = ngdpService.sendLetter(letterDTO.cpr, letterDTO.cvr, letterDTO.municipalityName, letterDTO.subject, letterDTO.content, letterDTO.attachments);

	    HttpHeaders responseHeaders = new HttpHeaders();
	    responseHeaders.setContentType(MediaType.TEXT_PLAIN);

		if (status.equals(Status.OK)) {
			return new ResponseEntity<>(HttpStatus.OK);
		}
		
		if (status.equals(Status.NOT_REGISTERED)) {
			return new ResponseEntity<>("Ikke tilmeldt e-boks!", responseHeaders, HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>("Besked kunne ikke sendes", responseHeaders, HttpStatus.BAD_REQUEST);
	}
}
