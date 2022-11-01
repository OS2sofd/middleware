package dk.digst.digital.post.memolib.xml.writer;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import dk.digst.digital.post.memolib.model.AdditionalDocument;
import dk.digst.digital.post.memolib.model.MessageBody;
import dk.digst.digital.post.memolib.model.TechnicalDocument;
import dk.digst.digital.post.memolib.writer.FileContentLoader;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import javax.xml.stream.XMLStreamException;
import org.codehaus.stax2.XMLStreamWriter2;

/** MessageBodyXmlWriter is responsible for serializing a {@link MessageBody}. */
public class MessageBodyWriter extends AbstractWriter {

  private final XmlMapper xmlMapper;
  private final OutputStream outputStream;
  private final FileContentLoader fileContentLoader;

  public MessageBodyWriter(
      XMLStreamWriter2 xmlStreamWriter,
      XmlMapper xmlMapper,
      FileContentLoader fileContentLoader,
      OutputStream outputStream) {

    super(xmlStreamWriter, "MessageBody");
    this.xmlMapper = xmlMapper;
    this.outputStream = outputStream;
    this.fileContentLoader = fileContentLoader;
  }

  /**
   * This method writes the {@link MessageBody}
   *
   * @param messageBody the messageBody
   * @throws XMLStreamException if a problem occurs when writing to the xml stream
   * @throws IOException if a low-level I/O problem occurs
   */
  public void write(MessageBody messageBody) throws XMLStreamException, IOException {
    ensureStartElementWritten();

    writeCreatedDateTime(messageBody.getCreatedDateTime());

    mainDocumentWriter().write(messageBody.getMainDocument());

    for (AdditionalDocument additionalDocument : messageBody.getAdditionalDocument()) {
      additionalDocumentWriter().write(additionalDocument);
    }

    for (TechnicalDocument technicalDocument : messageBody.getTechnicalDocument()) {
      technicalDocumentWriter().write(technicalDocument);
    }
  }

  private void writeCreatedDateTime(LocalDateTime createdDateTime) throws XMLStreamException {
    writeElement("createdDateTime", createdDateTime);
  }

  private AdditionalDocumentWriter additionalDocumentWriter() throws XMLStreamException {
    ensureStartElementWritten();
    return new AdditionalDocumentWriter(
        xmlStreamWriter, xmlMapper, fileContentLoader, outputStream);
  }

  private TechnicalDocumentWriter technicalDocumentWriter() throws XMLStreamException {
    ensureStartElementWritten();
    return new TechnicalDocumentWriter(xmlStreamWriter, xmlMapper, fileContentLoader, outputStream);
  }

  private MainDocumentWriter mainDocumentWriter() throws XMLStreamException {
    ensureStartElementWritten();
    return new MainDocumentWriter(xmlStreamWriter, xmlMapper, fileContentLoader, outputStream);
  }
}
