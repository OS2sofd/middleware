package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.GlobalLocationNumber;

public class GlobalLocationNumberBuilder {

  private long globalLocationNumber; // NOSONAR
  private String location;

  public static GlobalLocationNumberBuilder newBuilder() {
    return new GlobalLocationNumberBuilder();
  }

  public GlobalLocationNumberBuilder globalLocationNumber(long globalLocationNumber) {
    this.globalLocationNumber = globalLocationNumber;
    return this;
  }

  public GlobalLocationNumberBuilder location(String location) {
    this.location = location;
    return this;
  }

  public GlobalLocationNumber build() {
    return new GlobalLocationNumber(globalLocationNumber, location);
  }
}
