package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.File;
import dk.digst.digital.post.memolib.model.FileContent;

public class FileBuilder {

  private String encodingFormat;
  private String filename;
  private String inLanguage;
  private FileContent content;

  public static FileBuilder newBuilder() {
    return new FileBuilder();
  }

  public FileBuilder encodingFormat(String encodingFormat) {
    this.encodingFormat = encodingFormat;
    return this;
  }

  public FileBuilder filename(String filename) {
    this.filename = filename;
    return this;
  }

  public FileBuilder inLanguage(String inLanguage) {
    this.inLanguage = inLanguage;
    return this;
  }

  public FileBuilder content(FileContent content) {
    this.content = content;
    return this;
  }

  public File build() {
    return new File(encodingFormat, filename, inLanguage, content);
  }
}
