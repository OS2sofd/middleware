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
@JsonPropertyOrder({
    "messageUUID",
    "originalMessageDateTime",
    "originalSender",
    "originalContentResponsible",
    "contactPointId",
    "comment"
})
public class ForwardData implements MeMoClass {

  private UUID messageUUID;

  private LocalDateTime originalMessageDateTime;

  private String originalSender;

  private String originalContentResponsible;

  @JacksonXmlProperty(localName = "contactPointID")
  private String contactPointId;

  private String comment;
}
