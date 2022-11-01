package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.ExternalFileContent;
import dk.digst.digital.post.memolib.model.FileContent;
import dk.digst.digital.post.memolib.model.IncludedFileContent;

public class FileContentBuilder {

  private String content;
  private String location;
  private boolean base64encoded;

  public static FileContentBuilder newBuilder() {
    return new FileContentBuilder();
  }

  public FileContentBuilder base64Content(String content) {
    this.location = null;
    this.content = content;
    this.base64encoded = true;
    return this;
  }

  public FileContentBuilder location(String location) {
    this.content = null;
    this.location = location;
    this.base64encoded = false;
    return this;
  }

  public FileContent build() {
    if (content != null) {
      return new IncludedFileContent(content, base64encoded);
    } else if (location != null) {
      return new ExternalFileContent(location, base64encoded);
    } else {
      throw new IllegalStateException("either content or file path must be provided");
    }
  }
}
