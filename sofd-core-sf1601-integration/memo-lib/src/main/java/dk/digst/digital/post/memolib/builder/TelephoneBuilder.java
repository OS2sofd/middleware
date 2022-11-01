package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.Telephone;

public class TelephoneBuilder {

  private Integer telephoneNumber;
  private String relatedAgent;

  public static TelephoneBuilder newBuilder() {
    return new TelephoneBuilder();
  }

  public TelephoneBuilder telephoneNumber(Integer telephoneNumber) {
    this.telephoneNumber = telephoneNumber;
    return this;
  }

  public TelephoneBuilder relatedAgent(String relatedAgent) {
    this.relatedAgent = relatedAgent;
    return this;
  }

  public Telephone build() {
    return new Telephone(telephoneNumber, relatedAgent);
  }
}
