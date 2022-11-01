package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.CaseId;

public class CaseIdBuilder {

  private String caseId; // NOSONAR
  private String caseSystem;

  public static CaseIdBuilder newBuilder() {
    return new CaseIdBuilder();
  }

  public CaseIdBuilder caseId(String caseId) {
    this.caseId = caseId;
    return this;
  }

  public CaseIdBuilder caseSystem(String caseSystem) {
    this.caseSystem = caseSystem;
    return this;
  }

  public CaseId build() {
    return new CaseId(caseId, caseSystem);
  }
}
