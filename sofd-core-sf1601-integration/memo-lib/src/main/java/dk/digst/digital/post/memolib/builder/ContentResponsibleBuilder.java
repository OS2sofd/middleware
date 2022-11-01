package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.ContentResponsible;

public class ContentResponsibleBuilder {

  private String contentResponsibleId;
  private String label;

  public static ContentResponsibleBuilder newBuilder() {
    return new ContentResponsibleBuilder();
  }

  public ContentResponsibleBuilder contentResponsibleId(String contentResponsibleId) {
    this.contentResponsibleId = contentResponsibleId;
    return this;
  }

  public ContentResponsibleBuilder label(String label) {
    this.label = label;
    return this;
  }

  public ContentResponsible build() {
    return new ContentResponsible(contentResponsibleId, label);
  }
}
