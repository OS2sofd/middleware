package dk.digst.digital.post.memolib.xml.writer;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import dk.digst.digital.post.memolib.model.Action;
import dk.digst.digital.post.memolib.model.File;
import dk.digst.digital.post.memolib.model.MainDocument;
import dk.digst.digital.post.memolib.writer.FileContentLoader;
import java.io.IOException;
import java.io.OutputStream;
import javax.xml.stream.XMLStreamException;
import org.codehaus.stax2.XMLStreamWriter2;

/** MainDocumentXmlWriter is responsible for serializing a {@link MainDocument}. */
public class MainDocumentWriter extends AbstractDocumentWriter {

  public MainDocumentWriter(
      XMLStreamWriter2 xmlStreamWriter,
      XmlMapper xmlMapper,
      FileContentLoader fileContentLoader,
      OutputStream outputStream) {

    super(xmlStreamWriter, xmlMapper, outputStream, fileContentLoader, "MainDocument");
  }

  /**
   * The methods writes the MainDocument.
   *
   * @param mainDocument the main document
   * @throws IOException if a low-level I/O problem occurs
   * @throws XMLStreamException if a problem occurs when writing to the xml stream
   */
  public void write(MainDocument mainDocument) throws IOException, XMLStreamException {

    ensureStartElementWritten();
    writeMainDocumentID(mainDocument.getMainDocumentId());
    writeLabel(mainDocument.getLabel());

    for (File file : mainDocument.getFile()) {
      writeFile(file);
    }

    if (mainDocument.getAction() != null) {
      for (Action action : mainDocument.getAction()) {
        writeAction(action);
      }
    }
    xmlStreamWriter.writeEndElement();
  }

  private void writeAction(Action action) throws IOException, XMLStreamException {
    ensureStartElementWritten();
    xmlMapper.writeValue(xmlStreamWriter, action);
  }

  private void writeMainDocumentID(String mainDocumentId) throws XMLStreamException {
    writeElement("mainDocumentID", mainDocumentId);
  }
}
