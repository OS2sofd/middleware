package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.UnstructuredAddress;

public class UnstructuredAddressBuilder {

  private String unstructured;

  public static UnstructuredAddressBuilder newBuilder() {
    return new UnstructuredAddressBuilder();
  }

  public UnstructuredAddressBuilder unstructured(String unstructured) {
    this.unstructured = unstructured;
    return this;
  }

  public UnstructuredAddress build() {
    return new UnstructuredAddress(unstructured);
  }
}
