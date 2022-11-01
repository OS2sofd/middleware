package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.AdditionalContentData;

public class AdditionalContentDataBuilder {

  private String contentDataType;
  private String contentDataName;
  private String contentDataValue;

  public static AdditionalContentDataBuilder newBuilder() {
    return new AdditionalContentDataBuilder();
  }

  public AdditionalContentDataBuilder contentDataType(String contentDataType) {
    this.contentDataType = contentDataType;
    return this;
  }

  public AdditionalContentDataBuilder contentDataName(String contentDataName) {
    this.contentDataName = contentDataName;
    return this;
  }

  public AdditionalContentDataBuilder contentDataValue(String contentDataValue) {
    this.contentDataValue = contentDataValue;
    return this;
  }

  public AdditionalContentData build() {
    return new AdditionalContentData(contentDataType, contentDataName, contentDataValue);
  }
}
