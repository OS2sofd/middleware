package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.EntryPoint;

public class EntryPointBuilder {

  private String url;

  public static EntryPointBuilder newBuilder() {
    return new EntryPointBuilder();
  }

  public EntryPointBuilder url(String url) {
    this.url = url;
    return this;
  }

  public EntryPoint build() {
    return new EntryPoint(url);
  }
}
