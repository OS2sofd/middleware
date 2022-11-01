package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.AttentionData;
import dk.digst.digital.post.memolib.model.ContactPoint;
import dk.digst.digital.post.memolib.model.Recipient;

public class RecipientBuilder {

  private String recipientId;
  private String idType;
  private String idTypeLabel;
  private String label;
  private AttentionData attentionData;
  private ContactPoint contactPoint;

  public static RecipientBuilder newBuilder() {
    return new RecipientBuilder();
  }

  public RecipientBuilder recipientId(String recipientId) {
    this.recipientId = recipientId;
    return this;
  }

  public RecipientBuilder idType(String idType) {
    this.idType = idType;
    return this;
  }

  public RecipientBuilder idTypeLabel(String idTypeLabel) {
    this.idTypeLabel = idTypeLabel;
    return this;
  }

  public RecipientBuilder label(String label) {
    this.label = label;
    return this;
  }

  public RecipientBuilder attentionData(AttentionData attentionData) {
    this.attentionData = attentionData;
    return this;
  }

  public RecipientBuilder contactPoint(ContactPoint contactPoint) {
    this.contactPoint = contactPoint;
    return this;
  }

  public Recipient build() {
    return new Recipient(recipientId, idType, idTypeLabel, label, attentionData, contactPoint);
  }
}
