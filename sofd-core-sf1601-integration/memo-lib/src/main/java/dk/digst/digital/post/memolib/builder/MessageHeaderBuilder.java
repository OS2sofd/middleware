package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.ContentData;
import dk.digst.digital.post.memolib.model.ForwardData;
import dk.digst.digital.post.memolib.model.MessageHeader;
import dk.digst.digital.post.memolib.model.MessageType;
import dk.digst.digital.post.memolib.model.Recipient;
import dk.digst.digital.post.memolib.model.ReplyData;
import dk.digst.digital.post.memolib.model.Sender;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class MessageHeaderBuilder {

  private MessageType messageType;
  private UUID messageUUID;
  private String messageId;
  private String messageCode;
  private String label;
  private String notification;
  private String additionalNotification;
  private Boolean reply;
  private LocalDateTime replyByDateTime;
  private LocalDate doNotDeliverUntilDate;
  private Boolean mandatory;
  private Boolean legalNotification;
  private String postType;
  private Sender sender;
  private Recipient recipient;
  private ContentData contentData;
  private ForwardData forwardData;
  private List<ReplyData> replyData;

  public static MessageHeaderBuilder newBuilder() {
    return new MessageHeaderBuilder();
  }

  public MessageHeaderBuilder messageType(MessageType messageType) {
    this.messageType = messageType;
    return this;
  }

  public MessageHeaderBuilder messageUUID(UUID messageUUID) {
    this.messageUUID = messageUUID;
    return this;
  }

  public MessageHeaderBuilder messageId(String messageId) {
    this.messageId = messageId;
    return this;
  }

  public MessageHeaderBuilder messageCode(String messageCode) {
    this.messageCode = messageCode;
    return this;
  }

  public MessageHeaderBuilder label(String label) {
    this.label = label;
    return this;
  }

  public MessageHeaderBuilder notification(String notification) {
    this.notification = notification;
    return this;
  }

  public MessageHeaderBuilder additionalNotification(String additionalNotification) {
    this.additionalNotification = additionalNotification;
    return this;
  }

  public MessageHeaderBuilder reply(Boolean reply) {
    this.reply = reply;
    return this;
  }

  public MessageHeaderBuilder replyByDateTime(LocalDateTime replyByDateTime) {
    this.replyByDateTime = replyByDateTime;
    return this;
  }

  public MessageHeaderBuilder doNotDeliverUntilDate(LocalDate doNotDeliverUntilDate) {
    this.doNotDeliverUntilDate = doNotDeliverUntilDate;
    return this;
  }

  public MessageHeaderBuilder mandatory(Boolean mandatory) {
    this.mandatory = mandatory;
    return this;
  }

  public MessageHeaderBuilder legalNotification(Boolean legalNotification) {
    this.legalNotification = legalNotification;
    return this;
  }

  public MessageHeaderBuilder postType(String postType) {
    this.postType = postType;
    return this;
  }

  public MessageHeaderBuilder sender(Sender sender) {
    this.sender = sender;
    return this;
  }

  public MessageHeaderBuilder recipient(Recipient recipient) {
    this.recipient = recipient;
    return this;
  }

  public MessageHeaderBuilder contentData(ContentData contentData) {
    this.contentData = contentData;
    return this;
  }

  public MessageHeaderBuilder forwardData(ForwardData forwardData) {
    this.forwardData = forwardData;
    return this;
  }

  public MessageHeaderBuilder replyData(List<ReplyData> replyData) {
    this.replyData = replyData;
    return this;
  }

  public MessageHeaderBuilder addReplyData(ReplyData replyData) {
    this.replyData.add(replyData);
    return this;
  }

  public MessageHeader build() {
    return new MessageHeader(
        messageType,
        messageUUID,
        messageId,
        messageCode,
        label,
        notification,
        additionalNotification,
        reply,
        replyByDateTime,
        doNotDeliverUntilDate,
        mandatory,
        legalNotification,
        postType,
        sender,
        recipient,
        contentData,
        forwardData,
        replyData);
  }
}
