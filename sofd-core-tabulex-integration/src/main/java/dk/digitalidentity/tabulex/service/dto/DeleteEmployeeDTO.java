package dk.digitalidentity.tabulex.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeleteEmployeeDTO {
	private String cpr;
	private String skolekode;
}
