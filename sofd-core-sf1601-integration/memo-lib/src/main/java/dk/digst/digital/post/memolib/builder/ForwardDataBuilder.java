package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.ForwardData;
import java.time.LocalDateTime;
import java.util.UUID;

public class ForwardDataBuilder {

  private UUID messageUUID;
  private LocalDateTime originalMessageDateTime;
  private String originalSender;
  private String originalContentResponsible;
  private String contactPointId;
  private String comment;

  public static ForwardDataBuilder newBuilder() {
    return new ForwardDataBuilder();
  }

  public ForwardDataBuilder messageUUID(UUID messageUUID) {
    this.messageUUID = messageUUID;
    return this;
  }

  public ForwardDataBuilder originalSender(String originalSender) {
    this.originalSender = originalSender;
    return this;
  }

  public ForwardDataBuilder originalContentResponsible(String originalContentResponsible) {
    this.originalContentResponsible = originalContentResponsible;
    return this;
  }

  public ForwardDataBuilder contactPointId(String contactPointId) {
    this.contactPointId = contactPointId;
    return this;
  }

  public ForwardDataBuilder comment(String comment) {
    this.comment = comment;
    return this;
  }

  public ForwardDataBuilder originalMessageDateTime (LocalDateTime originalMessageDateTime) {
    this.originalMessageDateTime = originalMessageDateTime;
    return this;
  }

  public ForwardData build() {
    return new ForwardData(messageUUID, originalMessageDateTime, originalSender, originalContentResponsible, contactPointId, comment);
  }
}
