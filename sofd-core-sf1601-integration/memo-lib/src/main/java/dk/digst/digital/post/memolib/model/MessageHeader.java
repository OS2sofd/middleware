package dk.digst.digital.post.memolib.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
/* needed to control the order of 'messageId' element */
@JsonPropertyOrder({"messageType", "messageUUID", "messageId"})
public class MessageHeader implements MeMoClass {

  private MessageType messageType;

  private UUID messageUUID;

  @JacksonXmlProperty(localName = "messageID")
  private String messageId;

  private String messageCode;

  private String label;

  private String notification;

  private String additionalNotification;

  private Boolean reply;

  private LocalDateTime replyByDateTime;

  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate doNotDeliverUntilDate;

  private Boolean mandatory;

  private Boolean legalNotification;

  private String postType;

  @JacksonXmlProperty(localName = "Sender")
  private Sender sender;

  @JacksonXmlProperty(localName = "Recipient")
  private Recipient recipient;

  @JacksonXmlProperty(localName = "ContentData")
  private ContentData contentData;

  @JacksonXmlProperty(localName = "ForwardData")
  private ForwardData forwardData;

  @JacksonXmlProperty(localName = "ReplyData")
  private List<ReplyData> replyData;
}
