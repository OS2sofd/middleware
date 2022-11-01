package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.EidData;

public class EidDataBuilder {

  private String eid;
  private String label;

  public static EidDataBuilder newBuilder() {
    return new EidDataBuilder();
  }

  public EidDataBuilder eid(String eid) {
    this.eid = eid;
    return this;
  }

  public EidDataBuilder label(String label) {
    this.label = label;
    return this;
  }

  public EidData build() {
    return new EidData(eid, label);
  }
}
