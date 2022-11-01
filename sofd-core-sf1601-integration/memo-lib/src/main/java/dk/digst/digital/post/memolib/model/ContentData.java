package dk.digst.digital.post.memolib.model;

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
public class ContentData implements MeMoClass {

  @JacksonXmlProperty(localName = "CPRdata", namespace = Namespace.GRD)
  private CprData cprData;

  @JacksonXmlProperty(localName = "CVRdata", namespace = Namespace.GRD)
  private CvrData cvrData;

  @JacksonXmlProperty(localName = "MotorVehicle", namespace = Namespace.DMV)
  private MotorVehicle motorVehicle;

  @JacksonXmlProperty(localName = "PropertyNumber", namespace = Namespace.GRD)
  private PropertyNumber propertyNumber;

  @JacksonXmlProperty(localName = "CaseID")
  private CaseId caseId;

  @JacksonXmlProperty(localName = "KLEdata", namespace = Namespace.KLE)
  private KleData kleData;

  @JacksonXmlProperty(localName = "FORMdata", namespace = Namespace.FORM)
  private FormData formData;

  @JacksonXmlProperty(localName = "ProductionUnit", namespace = Namespace.GRD)
  private ProductionUnit productionUnit;

  @JacksonXmlProperty(localName = "Education", namespace = Namespace.UDD)
  private Education education;

  @JacksonXmlProperty(localName = "Address", namespace = Namespace.GRD)
  private Address address;

  @JacksonXmlProperty(localName = "UnstructuredAddress", namespace = Namespace.GRD)
  private UnstructuredAddress unstructuredAddress;

  @JacksonXmlProperty(localName = "AdditionalContentData")
  private List<AdditionalContentData> additionalContentData;
}
