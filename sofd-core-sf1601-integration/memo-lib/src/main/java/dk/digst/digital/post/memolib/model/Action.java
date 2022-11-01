package dk.digst.digital.post.memolib.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Action implements MeMoClass {

  private String label;

  private String actionCode;

  private LocalDateTime startDateTime;

  private LocalDateTime endDateTime;

  @JacksonXmlProperty(localName = "Reservation")
  private Reservation reservation;

  @JacksonXmlProperty(localName = "EntryPoint")
  private EntryPoint entryPoint;
}
