package dk.digst.digital.post.memolib.model;

import java.io.IOException;
import java.io.InputStream;

public class ExternalFileContent implements FileContent {

  private String location;
  private boolean base64encoded;

  public ExternalFileContent(String location, boolean base64encoded) {
    this.location = location;
    this.base64encoded = base64encoded;
  }

  public ExternalFileContent() {}

  @Override
  public String location() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  @Override
  public InputStream streamContent() throws IOException {
    throw new UnsupportedOperationException(
        "External file content can not be streamed. A FileContentResourceLoader must be used");
  }

  @Override
  public boolean canBeStreamed() {
    return false;
  }

  @Override
  public boolean isBase64encoded() {
    return base64encoded;
  }
}
