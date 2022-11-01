package dk.digst.digital.post.memolib.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonPropertyOrder({"contactGroup", "contactPointId"})
public class ContactPoint implements MeMoClass {

  private String contactGroup;

  @JacksonXmlProperty(localName = "contactPointID")
  private String contactPointId;

  private String label;

  @JacksonXmlProperty(localName = "ContactInfo")
  private List<ContactInfo> contactInfo;
}
