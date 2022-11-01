package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.AddressPoint;

public class AddressPointBuilder {

  private String geographicEastingMeasure;
  private String geographicNorthingMeasure;
  private String geographicHeightMeasure;

  public static AddressPointBuilder newBuilder() {
    return new AddressPointBuilder();
  }

  public AddressPointBuilder geographicEastingMeasure(String geographicEastingMeasure) {
    this.geographicEastingMeasure = geographicEastingMeasure;
    return this;
  }

  public AddressPointBuilder geographicNorthingMeasure(String geographicNorthingMeasure) {
    this.geographicNorthingMeasure = geographicNorthingMeasure;
    return this;
  }

  public AddressPointBuilder geographicHeightMeasure(String geographicHeightMeasure) {
    this.geographicHeightMeasure = geographicHeightMeasure;
    return this;
  }

  public AddressPoint build() {
    return new AddressPoint(
        geographicEastingMeasure, geographicNorthingMeasure, geographicHeightMeasure);
  }
}
