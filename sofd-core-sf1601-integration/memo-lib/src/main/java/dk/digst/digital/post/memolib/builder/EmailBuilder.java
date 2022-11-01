package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.Email;

public class EmailBuilder {

  private String emailAddress;
  private String relatedAgent;

  public static EmailBuilder newBuilder() {
    return new EmailBuilder();
  }

  public EmailBuilder emailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
    return this;
  }

  public EmailBuilder relatedAgent(String relatedAgent) {
    this.relatedAgent = relatedAgent;
    return this;
  }

  public Email build() {
    return new Email(emailAddress, relatedAgent);
  }
}
