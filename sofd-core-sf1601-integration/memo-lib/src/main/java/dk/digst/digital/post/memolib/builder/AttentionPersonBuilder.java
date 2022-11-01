package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.AttentionPerson;

public class AttentionPersonBuilder {

  private String personId;
  private String label;

  public static AttentionPersonBuilder newBuilder() {
    return new AttentionPersonBuilder();
  }

  public AttentionPersonBuilder personId(String personId) {
    this.personId = personId;
    return this;
  }

  public AttentionPersonBuilder label(String label) {
    this.label = label;
    return this;
  }

  public AttentionPerson build() {
    return new AttentionPerson(personId, label);
  }
}
