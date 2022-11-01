package dk.digst.digital.post.memolib.model;

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
public class UnstructuredAddress implements MeMoClass {

  private String unstructured;
}
