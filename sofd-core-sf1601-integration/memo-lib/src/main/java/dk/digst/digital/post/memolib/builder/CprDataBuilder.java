package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.CprData;

public class CprDataBuilder {

  private String cprNumber;
  private String name;

  public static CprDataBuilder newBuilder() {
    return new CprDataBuilder();
  }

  public CprDataBuilder cprNumber(String cprNumber) {
    this.cprNumber = cprNumber;
    return this;
  }

  public CprDataBuilder name(String name) {
    this.name = name;
    return this;
  }

  public CprData build() {
    return new CprData(cprNumber, name);
  }
}
