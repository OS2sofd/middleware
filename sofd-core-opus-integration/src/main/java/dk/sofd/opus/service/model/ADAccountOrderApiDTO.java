package dk.sofd.opus.service.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ADAccountOrderApiDTO {
	private String requesterUserId;
	private List<ADAccountOrderApiOrderDTO> orders;
}
