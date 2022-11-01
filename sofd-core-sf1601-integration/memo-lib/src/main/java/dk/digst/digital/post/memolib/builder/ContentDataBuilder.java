package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.AdditionalContentData;
import dk.digst.digital.post.memolib.model.Address;
import dk.digst.digital.post.memolib.model.CaseId;
import dk.digst.digital.post.memolib.model.ContentData;
import dk.digst.digital.post.memolib.model.CprData;
import dk.digst.digital.post.memolib.model.CvrData;
import dk.digst.digital.post.memolib.model.Education;
import dk.digst.digital.post.memolib.model.FormData;
import dk.digst.digital.post.memolib.model.KleData;
import dk.digst.digital.post.memolib.model.MotorVehicle;
import dk.digst.digital.post.memolib.model.ProductionUnit;
import dk.digst.digital.post.memolib.model.PropertyNumber;
import dk.digst.digital.post.memolib.model.UnstructuredAddress;
import java.util.ArrayList;
import java.util.List;

public class ContentDataBuilder {

  private CprData cprData;
  private CvrData cvrData;
  private MotorVehicle motorVehicle;
  private PropertyNumber propertyNumber;
  private CaseId caseId;
  private KleData kleData;
  private FormData formData;
  private ProductionUnit productionUnit;
  private Education education;
  private Address address;
  private UnstructuredAddress unstructuredAddress;

  private List<AdditionalContentData> additionalContentData = new ArrayList<>();

  public static ContentDataBuilder newBuilder() {
    return new ContentDataBuilder();
  }

  public ContentDataBuilder cprData(String cprNumber, String name) {
    this.cprData = new CprData(cprNumber, name);
    return this;
  }

  public ContentDataBuilder cvrData(String cvrNumber, String companyName) {
    this.cvrData = new CvrData(cvrNumber, companyName);
    return this;
  }

  public ContentDataBuilder motorVehicle(String licenseNumber, String chassisNumber) {
    this.motorVehicle = new MotorVehicle(licenseNumber, chassisNumber);
    return this;
  }

  public ContentDataBuilder propertyNumber(String propertyNumber) {
    this.propertyNumber = new PropertyNumber(propertyNumber);
    return this;
  }

  public ContentDataBuilder caseId(String caseId, String caseSystem) {
    this.caseId = new CaseId(caseId, caseSystem);
    return this;
  }

  public ContentDataBuilder kleData(
      String subjectKey, String version, String activityFacet, String label) {
    this.kleData = new KleData(subjectKey, version, activityFacet, label);
    return this;
  }

  public ContentDataBuilder formData(
      String taskKey, String version, String activityFacet, String label) {
    this.formData = new FormData(taskKey, version, activityFacet, label);
    return this;
  }

  public ContentDataBuilder productionUnit(int productionUnitNumber, String productionUnitName) {
    this.productionUnit = new ProductionUnit(productionUnitNumber, productionUnitName);
    return this;
  }

  public ContentDataBuilder education(String educationCode, String educationName) {
    this.education = new Education(educationCode, educationName);
    return this;
  }

  public ContentDataBuilder address(Address address) {
    this.address = address;
    return this;
  }

  public ContentDataBuilder unstructuredAddress(UnstructuredAddress unstructuredAddress) {
    this.unstructuredAddress = unstructuredAddress;
    return this;
  }

  public ContentDataBuilder additionalContentData(
      List<AdditionalContentData> additionalContentData) {
    this.additionalContentData = additionalContentData;
    return this;
  }

  public ContentDataBuilder addAdditionalContentData(AdditionalContentData additionalContentData) {
    this.additionalContentData.add(additionalContentData);
    return this;
  }

  public ContentData build() {
    return new ContentData(
        cprData,
        cvrData,
        motorVehicle,
        propertyNumber,
        caseId,
        kleData,
        formData,
        productionUnit,
        education,
        address,
        unstructuredAddress,
        additionalContentData);
  }
}
