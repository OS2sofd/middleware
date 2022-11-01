package dk.digst.digital.post.memolib.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonPropertyOrder({"recipientID"})
public class Recipient implements MeMoClass {

  @JacksonXmlProperty(localName = "recipientID")
  private String recipientId;

  private String idType;
  private String idTypeLabel;
  private String label;

  @JacksonXmlProperty(localName = "AttentionData")
  private AttentionData attentionData;

  @JacksonXmlProperty(localName = "ContactPoint")
  private ContactPoint contactPoint;
}
