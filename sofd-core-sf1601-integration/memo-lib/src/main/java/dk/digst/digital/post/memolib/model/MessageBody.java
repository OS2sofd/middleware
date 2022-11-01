package dk.digst.digital.post.memolib.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MessageBody implements MeMoClass {

  private LocalDateTime createdDateTime;

  @JacksonXmlProperty(localName = "MainDocument")
  private MainDocument mainDocument;

  @JacksonXmlProperty(localName = "AdditionalDocument")
  private List<AdditionalDocument> additionalDocument = new ArrayList<>();

  @JacksonXmlProperty(localName = "TechnicalDocument")
  private List<TechnicalDocument> technicalDocument = new ArrayList<>();
}
