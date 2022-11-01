package dk.digst.digital.post.memolib.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static dk.digst.digital.post.memolib.model.Namespace.DMV;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(namespace = DMV)
public class MotorVehicle implements MeMoClass {

  private String licenseNumber;
  private String chassisNumber;
}
