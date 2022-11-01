package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.ContactInfo;
import dk.digst.digital.post.memolib.model.ContactPoint;
import java.util.ArrayList;
import java.util.List;

public class ContactPointBuilder {

  private String contactGroup;
  private String contactPointId;
  private String label;
  private List<ContactInfo> contactInfo = new ArrayList<>();

  public static ContactPointBuilder newBuilder() {
    return new ContactPointBuilder();
  }

  public ContactPointBuilder contactGroup(String contactGroup) {
    this.contactGroup = contactGroup;
    return this;
  }

  public ContactPointBuilder contactPointId(String contactPointId) {
    this.contactPointId = contactPointId;
    return this;
  }

  public ContactPointBuilder label(String label) {
    this.label = label;
    return this;
  }

  public ContactPointBuilder contactInfo(List<ContactInfo> contactInfo) {
    this.contactInfo = contactInfo;
    return this;
  }

  public ContactPoint build() {
    return new ContactPoint(contactGroup, contactPointId, label, contactInfo);
  }
}
