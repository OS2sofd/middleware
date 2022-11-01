package dk.digst.digital.post.memolib.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JacksonXmlRootElement(namespace = Namespace.GRD)
public class AddressPoint implements MeMoClass {

  private String geographicEastingMeasure;
  private String geographicNorthingMeasure;
  private String geographicHeightMeasure;
}
