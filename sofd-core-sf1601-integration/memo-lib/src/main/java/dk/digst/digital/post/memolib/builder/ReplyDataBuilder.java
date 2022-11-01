package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.AdditionalReplyData;
import dk.digst.digital.post.memolib.model.ReplyData;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReplyDataBuilder {

  private String messageId;
  private UUID messageUUID;
  private UUID replyUUID;
  private String senderId;
  private String recipientId;
  private String caseId;
  private String contactPointId;
  private String generatingSystemId;
  private String comment;

  private List<AdditionalReplyData> additionalReplyData = new ArrayList<>();

  public static ReplyDataBuilder newBuilder() {
    return new ReplyDataBuilder();
  }

  public ReplyDataBuilder messageId(String messageId) {
    this.messageId = messageId;
    return this;
  }

  public ReplyDataBuilder messageUUID(UUID messageUUID) {
    this.messageUUID = messageUUID;
    return this;
  }

  public ReplyDataBuilder replyUUID(UUID replyUUID) {
    this.replyUUID = replyUUID;
    return this;
  }

  public ReplyDataBuilder senderId(String senderId) {
    this.senderId = senderId;
    return this;
  }

  public ReplyDataBuilder recipientId(String recipientId) {
    this.recipientId = recipientId;
    return this;
  }

  public ReplyDataBuilder caseId(String caseId) {
    this.caseId = caseId;
    return this;
  }

  public ReplyDataBuilder contactPointId(String contactPointId) {
    this.contactPointId = contactPointId;
    return this;
  }

  public ReplyDataBuilder generatingSystemId(String generatingSystemId) {
    this.generatingSystemId = generatingSystemId;
    return this;
  }

  public ReplyDataBuilder comment(String comment) {
    this.comment = comment;
    return this;
  }

  public ReplyDataBuilder additionalReplyData(
      List<AdditionalReplyData> additionalReplyData) {
    this.additionalReplyData = additionalReplyData;
    return this;
  }

  public ReplyDataBuilder addAdditionalReplyData(AdditionalReplyData additionalReplyData) {
    this.additionalReplyData.add(additionalReplyData);
    return this;
  }

  public ReplyData build() {
    return new ReplyData(
        messageId,
        messageUUID,
        replyUUID,
        senderId,
        recipientId,
        caseId,
        contactPointId,
        generatingSystemId,
        comment,
        additionalReplyData);
  }
}
