package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.Address;
import dk.digst.digital.post.memolib.model.AttentionData;
import dk.digst.digital.post.memolib.model.AttentionPerson;
import dk.digst.digital.post.memolib.model.ContentResponsible;
import dk.digst.digital.post.memolib.model.EidData;
import dk.digst.digital.post.memolib.model.Email;
import dk.digst.digital.post.memolib.model.GeneratingSystem;
import dk.digst.digital.post.memolib.model.GlobalLocationNumber;
import dk.digst.digital.post.memolib.model.ProductionUnit;
import dk.digst.digital.post.memolib.model.SeNumber;
import dk.digst.digital.post.memolib.model.SorData;
import dk.digst.digital.post.memolib.model.Telephone;
import dk.digst.digital.post.memolib.model.UnstructuredAddress;

public class AttentionDataBuilder {

  private AttentionPerson attentionPerson;
  private ProductionUnit productionUnit;
  private GlobalLocationNumber globalLocationNumber;
  private Email email;
  private SeNumber senumber;
  private Telephone telephone;
  private EidData eidData;
  private ContentResponsible contentResponsible;
  private GeneratingSystem generatingSystem;
  private SorData sorData;
  private Address address;
  private UnstructuredAddress unstructuredAddress;

  public static AttentionDataBuilder newBuilder() {
    return new AttentionDataBuilder();
  }

  public AttentionDataBuilder attentionPerson(AttentionPerson attentionPerson) {
    this.attentionPerson = attentionPerson;
    return this;
  }

  public AttentionDataBuilder productionUnit(ProductionUnit productionUnit) {
    this.productionUnit = productionUnit;
    return this;
  }

  public AttentionDataBuilder globalLocationNumber(GlobalLocationNumber globalLocationNumber) {
    this.globalLocationNumber = globalLocationNumber;
    return this;
  }

  public AttentionDataBuilder email(Email email) {
    this.email = email;
    return this;
  }

  public AttentionDataBuilder senumber(SeNumber senumber) {
    this.senumber = senumber;
    return this;
  }

  public AttentionDataBuilder telephone(Telephone telephone) {
    this.telephone = telephone;
    return this;
  }

  public AttentionDataBuilder ridData(EidData eidData) {
    this.eidData = eidData;
    return this;
  }

  public AttentionDataBuilder contentResponsible(ContentResponsible contentResponsible) {
    this.contentResponsible = contentResponsible;
    return this;
  }

  public AttentionDataBuilder generatingSystem(GeneratingSystem generatingSystem) {
    this.generatingSystem = generatingSystem;
    return this;
  }

  public AttentionDataBuilder address(Address address) {
    this.address = address;
    return this;
  }

  public AttentionDataBuilder sorData(SorData sorData) {
    this.sorData = sorData;
    return this;
  }

  public AttentionDataBuilder unstructuredAddress(UnstructuredAddress unstructuredAddress) {
    this.unstructuredAddress = unstructuredAddress;
    return this;
  }

  public AttentionData build() {
    return new AttentionData(
        attentionPerson,
        productionUnit,
        globalLocationNumber,
        email,
        senumber,
        telephone,
        eidData,
        contentResponsible,
        generatingSystem,
        sorData,
        address,
        unstructuredAddress);
  }
}
