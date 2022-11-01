package dk.digst.digital.post.memolib.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static dk.digst.digital.post.memolib.model.Namespace.GLN;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JacksonXmlRootElement(namespace = GLN)
public class GlobalLocationNumber implements MeMoClass {

  private long globalLocationNumber; // NOSONAR
  private String location;
}
