package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.Action;
import dk.digst.digital.post.memolib.model.AdditionalDocument;
import dk.digst.digital.post.memolib.model.File;
import java.util.ArrayList;
import java.util.List;

public class AdditionalDocumentBuilder {

  private String additionalDocumentId;
  private String label;
  private List<File> file = new ArrayList<>();
  private List<Action> action = new ArrayList<>();

  public static AdditionalDocumentBuilder newBuilder() {
    return new AdditionalDocumentBuilder();
  }

  public AdditionalDocumentBuilder additionalDocumentId(String additionalDocumentId) {
    this.additionalDocumentId = additionalDocumentId;
    return this;
  }

  public AdditionalDocumentBuilder label(String label) {
    this.label = label;
    return this;
  }

  public AdditionalDocumentBuilder file(List<File> file) {
    this.file = file;
    return this;
  }

  public AdditionalDocumentBuilder addFile(File file) {
    this.file.add(file);
    return this;
  }

  public AdditionalDocumentBuilder action(List<Action> action) {
    this.action = action;
    return this;
  }

  public AdditionalDocumentBuilder addAction(Action action) {
    this.action.add(action);
    return this;
  }

  public AdditionalDocument build() {
    return new AdditionalDocument(additionalDocumentId, label, file, action);
  }
}
