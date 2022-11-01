package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.SorData;

public class SorDataBuilder {

  private String sorIdentifier;
  private String entryName;

  public static SorDataBuilder newBuilder() {
    return new SorDataBuilder();
  }

  public SorDataBuilder sorIdentifier(String sorIdentifier) {
    this.sorIdentifier = sorIdentifier;
    return this;
  }

  public SorDataBuilder entryName(String entryName) {
    this.entryName = entryName;
    return this;
  }

  public SorData build() {
    return new SorData(sorIdentifier, entryName);
  }
}
