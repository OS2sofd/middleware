package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.SeNumber;

public class SeNumberBuilder {

  private String seNumber;
  private String companyName;

  public static SeNumberBuilder newBuilder() {
    return new SeNumberBuilder();
  }

  public SeNumberBuilder seNumber(String seNumber) {
    this.seNumber = seNumber;
    return this;
  }

  public SeNumberBuilder companyName(String companyName) {
    this.companyName = companyName;
    return this;
  }

  public SeNumber build() {
    return new SeNumber(seNumber, companyName);
  }
}
