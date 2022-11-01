package dk.digst.digital.post.memolib.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonPropertyOrder({
  "messageId",
  "messageUUID",
  "replyUUID",
  "senderId",
  "recipientId",
  "caseId",
  "contactPointId",
  "generatingSystemId"
})
public class ReplyData implements MeMoClass {

  @JacksonXmlProperty(localName = "messageID")
  private String messageId;

  private UUID messageUUID;

  private UUID replyUUID;

  @JacksonXmlProperty(localName = "senderID")
  private String senderId;

  @JacksonXmlProperty(localName = "recipientID")
  private String recipientId;

  @JacksonXmlProperty(localName = "caseID")
  private String caseId;

  @JacksonXmlProperty(localName = "contactPointID")
  private String contactPointId;

  @JacksonXmlProperty(localName = "generatingSystemID")
  private String generatingSystemId;

  private String comment;

  @JacksonXmlProperty(localName = "AdditionalReplyData")
  private List<AdditionalReplyData> additionalReplyData;
}
