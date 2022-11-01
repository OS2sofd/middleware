package dk.digst.digital.post.memolib.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AdditionalDocument implements MeMoClass {

  @JacksonXmlProperty(localName = "additionalDocumentID")
  private String additionalDocumentId;

  private String label;

  @JacksonXmlProperty(localName = "File")
  private List<File> file;

  @JacksonXmlProperty(localName = "Action")
  private List<Action> action;
}
