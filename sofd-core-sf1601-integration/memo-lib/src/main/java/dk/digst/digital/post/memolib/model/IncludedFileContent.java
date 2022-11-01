package dk.digst.digital.post.memolib.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class IncludedFileContent implements FileContent {

  private byte[] content;
  private boolean base64encoded;

  public IncludedFileContent(String content, boolean base64encoded) {
    this.content = content.getBytes();
    this.base64encoded = base64encoded;
  }

  public IncludedFileContent() {}

  public IncludedFileContent(String content) {
    this.content = content.getBytes();
    this.base64encoded = true;
  }

  public void setContent(byte[] content) {
    this.content = content;
  }

  public byte[] getContent() {
    return content;
  }

  @Override
  public InputStream streamContent() {
    return new ByteArrayInputStream(content);
  }

  @Override
  public String location() {
    return null;
  }

  @Override
  public boolean canBeStreamed() {
    return true;
  }

  @Override
  public boolean isBase64encoded() {
    return base64encoded;
  }
}
