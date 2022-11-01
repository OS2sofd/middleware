package dk.digst.digital.post.memolib.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static dk.digst.digital.post.memolib.model.Namespace.GRD;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JacksonXmlRootElement(namespace = GRD)
public class Address implements MeMoClass {

  private String id;
  private String addressLabel;
  private String houseNumber;
  private String door;
  private String floor;
  private String co;
  private String zipCode;
  private String city;
  private String country;

  @JacksonXmlProperty(localName = "AddressPoint")
  private AddressPoint addressPoint;
}
