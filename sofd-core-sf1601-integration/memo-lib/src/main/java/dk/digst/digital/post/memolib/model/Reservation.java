package dk.digst.digital.post.memolib.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
/* needed to control the order of 'abstract' element */
@JsonPropertyOrder({"description", "reservationUUID", "reservationAbstract"})
public class Reservation implements MeMoClass {

  private String description;
  private UUID reservationUUID;
  /* called abstract in the MeMo format, but has been renamed to reservationAbstract. */
  @JacksonXmlProperty(localName = "abstract")
  private String reservationAbstract;

  private String location;

  private LocalDateTime startDateTime;

  private LocalDateTime endDateTime;

  private String organizerMail;
  private String organizerName;
}
