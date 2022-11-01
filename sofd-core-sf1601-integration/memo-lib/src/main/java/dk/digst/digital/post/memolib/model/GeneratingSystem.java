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
@JsonPropertyOrder({"generatingSystemId"})
public class GeneratingSystem implements MeMoClass {

  @JacksonXmlProperty(localName = "generatingSystemID")
  private String generatingSystemId;

  private String label;
}
