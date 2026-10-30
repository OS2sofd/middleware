package dk.digitalidentity.sofd.wizkids.service.wizkid;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class WizkidResponse {
	private long pageNumber;
	private long pageSize;
	private long totalNumberOfPages;
	private long totalNumberOfRecords;
	private List<WizkidUser> items;
}
