package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.AttentionData;
import dk.digst.digital.post.memolib.model.ContactPoint;
import dk.digst.digital.post.memolib.model.Sender;

public class SenderBuilder {

  private String senderId;
  private String idType;
  private String idTypeLabel;
  private String label;
  private AttentionData attentionData;
  private ContactPoint contactPoint;

  public static SenderBuilder newBuilder() {
    return new SenderBuilder();
  }

  public SenderBuilder senderId(String senderId) {
    this.senderId = senderId;
    return this;
  }

  public SenderBuilder idType(String idType) {
    this.idType = idType;
    return this;
  }

  public SenderBuilder idTypeLabel(String idTypeLabel) {
    this.idTypeLabel = idTypeLabel;
    return this;
  }

  public SenderBuilder label(String label) {
    this.label = label;
    return this;
  }

  public SenderBuilder attentionData(AttentionData attentionData) {
    this.attentionData = attentionData;
    return this;
  }

  public SenderBuilder contactPoint(ContactPoint contactPoint) {
    this.contactPoint = contactPoint;
    return this;
  }

  public Sender build() {
    return new Sender(senderId, idType, idTypeLabel, label, attentionData, contactPoint);
  }
}
