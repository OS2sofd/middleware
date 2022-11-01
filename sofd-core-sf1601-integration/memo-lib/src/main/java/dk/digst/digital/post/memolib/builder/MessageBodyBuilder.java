package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.AdditionalDocument;
import dk.digst.digital.post.memolib.model.MainDocument;
import dk.digst.digital.post.memolib.model.MessageBody;
import dk.digst.digital.post.memolib.model.TechnicalDocument;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageBodyBuilder {

  private LocalDateTime createdDateTime;
  private MainDocument mainDocument;
  private List<AdditionalDocument> additionalDocuments = new ArrayList<>();
  private List<TechnicalDocument> technicalDocuments = new ArrayList<>();

  public static MessageBodyBuilder newBuilder() {
    return new MessageBodyBuilder();
  }

  public MessageBodyBuilder createdDateTime(LocalDateTime createdDateTime) {
    this.createdDateTime = createdDateTime;
    return this;
  }

  public MessageBodyBuilder mainDocument(MainDocument mainDocument) {
    this.mainDocument = mainDocument;
    return this;
  }

  public MessageBodyBuilder additionalDocuments(List<AdditionalDocument> additionalDocuments) {
    this.additionalDocuments = additionalDocuments;
    return this;
  }

  public MessageBodyBuilder addAdditionalDocument(AdditionalDocument additionalDocument) {
    this.additionalDocuments.add(additionalDocument);
    return this;
  }

  public MessageBodyBuilder technicalDocuments(List<TechnicalDocument> technicalDocuments) {
    this.technicalDocuments = technicalDocuments;
    return this;
  }

  public MessageBodyBuilder addTechnicalDocument(TechnicalDocument technicalDocument) {
    this.technicalDocuments.add(technicalDocument);
    return this;
  }

  public MessageBody build() {
    return new MessageBody(createdDateTime, mainDocument, additionalDocuments, technicalDocuments);
  }
}
