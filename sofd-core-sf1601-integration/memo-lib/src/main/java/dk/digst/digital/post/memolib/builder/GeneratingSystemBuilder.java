package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.GeneratingSystem;

public class GeneratingSystemBuilder {

  private String generatingSystemId;
  private String label;

  public static GeneratingSystemBuilder newBuilder() {
    return new GeneratingSystemBuilder();
  }

  public GeneratingSystemBuilder generatingSystemId(String generatingSystemId) {
    this.generatingSystemId = generatingSystemId;
    return this;
  }

  public GeneratingSystemBuilder label(String label) {
    this.label = label;
    return this;
  }

  public GeneratingSystem build() {
    return new GeneratingSystem(generatingSystemId, label);
  }
}
