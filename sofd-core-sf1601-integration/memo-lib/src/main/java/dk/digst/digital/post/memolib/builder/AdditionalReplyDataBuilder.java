package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.AdditionalReplyData;

public class AdditionalReplyDataBuilder {

  private String label;
  private String value;

  public static AdditionalReplyDataBuilder newBuilder() {
    return new AdditionalReplyDataBuilder();
  }

  public AdditionalReplyDataBuilder label(String label) {
    this.label = label;
    return this;
  }

  public AdditionalReplyDataBuilder value(String value) {
    this.value = value;
    return this;
  }

  public AdditionalReplyData build() {
    return new AdditionalReplyData(label, value);
  }
}
