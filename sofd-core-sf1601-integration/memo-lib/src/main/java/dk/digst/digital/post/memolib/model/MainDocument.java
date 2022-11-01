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
public class MainDocument implements MeMoClass {

  @JacksonXmlProperty(localName = "mainDocumentID")
  private String mainDocumentId;

  private String label;

  @JacksonXmlProperty(localName = "File")
  private List<File> file;

  @JacksonXmlProperty(localName = "Action")
  private List<Action> action;
}
