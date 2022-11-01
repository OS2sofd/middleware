package dk.digst.digital.post.memolib.xml.writer;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import dk.digst.digital.post.memolib.model.File;
import dk.digst.digital.post.memolib.model.TechnicalDocument;
import dk.digst.digital.post.memolib.writer.FileContentLoader;
import java.io.IOException;
import java.io.OutputStream;
import javax.xml.stream.XMLStreamException;
import org.codehaus.stax2.XMLStreamWriter2;

/**
 * TechnicalDocumentXmlWriter is responsible for serializing a {@link TechnicalDocument}.
 */
public class TechnicalDocumentWriter extends AbstractDocumentWriter {

  public TechnicalDocumentWriter(
      XMLStreamWriter2 xmlStreamWriter,
      XmlMapper xmlMapper,
      FileContentLoader fileContentLoader,
      OutputStream outputStream) {

    super(xmlStreamWriter, xmlMapper, outputStream, fileContentLoader, "TechnicalDocument");
  }

  /**
   * The methods writes the TechnicalDocument.
   *
   * @param technicalDocument the additional document
   * @throws IOException if a low-level I/O problem occurs
   * @throws XMLStreamException if a problem occurs when writing to the xml stream
   */
  public void write(TechnicalDocument technicalDocument) throws IOException, XMLStreamException {

    ensureStartElementWritten();
    writeTechnicalDocumentID(technicalDocument.getTechnicalDocumentId());
    writeLabel(technicalDocument.getLabel());

    for (File file : technicalDocument.getFile()) {
      writeFile(file);
    }

    xmlStreamWriter.writeEndElement();
  }

  private void writeTechnicalDocumentID(String technicalDocumentId) throws XMLStreamException {
    writeElement("technicalDocumentID", technicalDocumentId);
  }
}
