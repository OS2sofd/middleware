package dk.digst.digital.post.memolib.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Message implements MeMoClass {

  private final BigDecimal memoVersion = MemoVersion.MEMO_VERSION;

  private final String memoSchVersion = MemoVersion.MEMO_SCH_VERSION;

  @JacksonXmlProperty(localName = "MessageHeader")
  private MessageHeader messageHeader;

  @JacksonXmlProperty(localName = "MessageBody")
  private MessageBody messageBody;


}
