package dk.digst.digital.post.memolib.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AttentionData implements MeMoClass {

  @JacksonXmlProperty(localName = "AttentionPerson")
  private AttentionPerson attentionPerson;

  @JacksonXmlProperty(localName = "ProductionUnit", namespace = Namespace.GRD)
  private ProductionUnit productionUnit;

  @JacksonXmlProperty(localName = "GlobalLocationNumber", namespace = Namespace.GLN)
  private GlobalLocationNumber globalLocationNumber;

  @JacksonXmlProperty(localName = "EMail")
  private Email email;

  @JacksonXmlProperty(localName = "SEnumber", namespace = Namespace.GRD)
  private SeNumber seNumber;

  @JacksonXmlProperty(localName = "Telephone")
  private Telephone telephone;

  @JacksonXmlProperty(localName = "EID", namespace = Namespace.GRD)
  private EidData eidData;

  @JacksonXmlProperty(localName = "ContentResponsible")
  private ContentResponsible contentResponsible;

  @JacksonXmlProperty(localName = "GeneratingSystem")
  private GeneratingSystem generatingSystem;

  @JacksonXmlProperty(localName = "SORdata", namespace = Namespace.SOR)
  private SorData sorData;

  @JacksonXmlProperty(localName = "Address", namespace = Namespace.GRD)
  private Address address;

  @JacksonXmlProperty(localName = "UnstructuredAddress", namespace = Namespace.GRD)
  private UnstructuredAddress unstructuredAddress;
}
