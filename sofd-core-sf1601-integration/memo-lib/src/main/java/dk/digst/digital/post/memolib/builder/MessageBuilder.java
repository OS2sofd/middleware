package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.Message;
import dk.digst.digital.post.memolib.model.MessageBody;
import dk.digst.digital.post.memolib.model.MessageHeader;

public class MessageBuilder {

  private MessageHeader messageHeader;

  private MessageBody messageBody;

  public static MessageBuilder newBuilder() {
    return new MessageBuilder();
  }

  public MessageBuilder messageHeader(MessageHeader messageHeader) {
    this.messageHeader = messageHeader;
    return this;
  }

  public MessageBuilder messageBody(MessageBody messageBody) {
    this.messageBody = messageBody;
    return this;
  }

  public Message build() {
    return new Message(messageHeader, messageBody);
  }
}
