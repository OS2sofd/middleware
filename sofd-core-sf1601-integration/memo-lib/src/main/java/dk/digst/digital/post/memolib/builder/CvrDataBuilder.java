package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.CvrData;

public class CvrDataBuilder {

  private String cvrNumber;
  private String companyName;

  public static CvrDataBuilder newBuilder() {
    return new CvrDataBuilder();
  }

  public CvrDataBuilder cvrNumber(String cvrNumber) {
    this.cvrNumber = cvrNumber;
    return this;
  }

  public CvrDataBuilder companyName(String companyName) {
    this.companyName = companyName;
    return this;
  }

  public CvrData build() {
    return new CvrData(cvrNumber, companyName);
  }
}
