package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.ContactInfo;

public class ContactInfoBuilder {

  private String label;
  private String value;

  public static ContactInfoBuilder newBuilder() {
    return new ContactInfoBuilder();
  }

  public ContactInfoBuilder label(String label) {
    this.label = label;
    return this;
  }

  public ContactInfoBuilder value(String value) {
    this.value = value;
    return this;
  }

  public ContactInfo build() {
    return new ContactInfo(label, value);
  }
}
