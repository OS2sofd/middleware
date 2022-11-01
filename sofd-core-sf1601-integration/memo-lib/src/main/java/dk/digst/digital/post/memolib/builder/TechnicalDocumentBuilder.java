package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.File;
import dk.digst.digital.post.memolib.model.TechnicalDocument;
import java.util.ArrayList;
import java.util.List;

public class TechnicalDocumentBuilder {

  private String technicalDocumentId;
  private String label;
  private List<File> file = new ArrayList<>();

  public static TechnicalDocumentBuilder newBuilder() {
    return new TechnicalDocumentBuilder();
  }

  public TechnicalDocumentBuilder technicalDocumentId(String technicalDocumentId) {
    this.technicalDocumentId = technicalDocumentId;
    return this;
  }

  public TechnicalDocumentBuilder label(String label) {
    this.label = label;
    return this;
  }

  public TechnicalDocumentBuilder file(List<File> file) {
    this.file = file;
    return this;
  }

  public TechnicalDocumentBuilder addFile(File file) {
    this.file.add(file);
    return this;
  }

  public TechnicalDocument build() {
    return new TechnicalDocument(technicalDocumentId, label, file);
  }
}
