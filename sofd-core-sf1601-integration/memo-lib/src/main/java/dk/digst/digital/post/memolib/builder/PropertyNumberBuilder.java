package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.PropertyNumber;

public class PropertyNumberBuilder {

  private String propertyNumber;

  public static PropertyNumberBuilder newBuilder() {
    return new PropertyNumberBuilder();
  }

  public PropertyNumberBuilder propertyNumber(String propertyNumber) {
    this.propertyNumber = propertyNumber;
    return this;
  }

  public PropertyNumber build() {
    return new PropertyNumber(propertyNumber);
  }
}
