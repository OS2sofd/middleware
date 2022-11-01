package dk.digst.digital.post.memolib.xml.writer;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import dk.digst.digital.post.memolib.model.Action;
import dk.digst.digital.post.memolib.model.AdditionalDocument;
import dk.digst.digital.post.memolib.model.File;
import dk.digst.digital.post.memolib.writer.FileContentLoader;
import java.io.IOException;
import java.io.OutputStream;
import javax.xml.stream.XMLStreamException;
import org.codehaus.stax2.XMLStreamWriter2;

/** AdditionalDocumentXmlWriter is responsible for serializing a {@link AdditionalDocument}. */
public class AdditionalDocumentWriter extends AbstractDocumentWriter {

  public AdditionalDocumentWriter(
      XMLStreamWriter2 xmlStreamWriter,
      XmlMapper xmlMapper,
      FileContentLoader fileContentLoader,
      OutputStream outputStream) {

    super(xmlStreamWriter, xmlMapper, outputStream, fileContentLoader, "AdditionalDocument");
  }

  /**
   * The methods writes the AdditionalDocument.
   *
   * @param additionalDocument the additional document
   * @throws IOException if a low-level I/O problem occurs
   * @throws XMLStreamException if a problem occurs when writing to the xml stream
   */
  public void write(AdditionalDocument additionalDocument) throws IOException, XMLStreamException {

    ensureStartElementWritten();
    writeAdditionalDocumentID(additionalDocument.getAdditionalDocumentId());
    writeLabel(additionalDocument.getLabel());

    for (File file : additionalDocument.getFile()) {
      writeFile(file);
    }

    if (additionalDocument.getAction() != null) {
      for (Action action : additionalDocument.getAction()) {
        writeAction(action);
      }
    }
    xmlStreamWriter.writeEndElement();
  }

  private void writeAction(Action action) throws IOException, XMLStreamException {
    ensureStartElementWritten();
    xmlMapper.writeValue(xmlStreamWriter, action);
  }

  private void writeAdditionalDocumentID(String additionalDocumentId) throws XMLStreamException {
    writeElement("additionalDocumentID", additionalDocumentId);
  }
}
