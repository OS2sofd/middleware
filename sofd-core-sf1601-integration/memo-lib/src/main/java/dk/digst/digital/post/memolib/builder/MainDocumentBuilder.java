package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.Action;
import dk.digst.digital.post.memolib.model.File;
import dk.digst.digital.post.memolib.model.MainDocument;
import java.util.ArrayList;
import java.util.List;

public class MainDocumentBuilder {

  private String mainDocumentId;
  private String label;
  private List<File> file = new ArrayList<>();
  private List<Action> action = new ArrayList<>();

  public static MainDocumentBuilder newBuilder() {
    return new MainDocumentBuilder();
  }

  public MainDocumentBuilder mainDocumentId(String mainDocumentId) {
    this.mainDocumentId = mainDocumentId;
    return this;
  }

  public MainDocumentBuilder label(String label) {
    this.label = label;
    return this;
  }

  public MainDocumentBuilder file(List<File> file) {
    this.file = file;
    return this;
  }

  public MainDocumentBuilder addFile(File file) {
    this.file.add(file);
    return this;
  }

  public MainDocumentBuilder action(List<Action> action) {
    this.action = action;
    return this;
  }

  public MainDocumentBuilder addAction(Action action) {
    this.action.add(action);
    return this;
  }

  public MainDocument build() {
    return new MainDocument(mainDocumentId, label, file, action);
  }
}
