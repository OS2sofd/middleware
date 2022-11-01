package dk.digst.digital.post.memolib.xml.writer;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import dk.digst.digital.post.memolib.model.File;
import dk.digst.digital.post.memolib.util.Base64StreamEncoder;
import dk.digst.digital.post.memolib.writer.FileContentLoader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import javax.xml.stream.XMLStreamException;
import org.codehaus.stax2.XMLStreamWriter2;

import static dk.digst.digital.post.memolib.model.Namespace.MEMO;

/**
 * AbstractDocumentXmlWriter is the base class for {@link MainDocumentWriter}, {@link
 * AdditionalDocumentWriter} and {@link TechnicalDocumentWriter}.
 */
public abstract class AbstractDocumentWriter extends AbstractWriter {

  protected final XmlMapper xmlMapper;
  protected final OutputStream outputStream;
  protected final FileContentLoader fileContentLoader;

  public AbstractDocumentWriter(
      XMLStreamWriter2 xmlStreamWriter,
      XmlMapper xmlMapper,
      OutputStream outputStream,
      FileContentLoader fileContentLoader,
      String elementName) {

    super(xmlStreamWriter, elementName);
    this.xmlMapper = xmlMapper;
    this.outputStream = outputStream;
    this.fileContentLoader = fileContentLoader;
  }

  protected void writeLabel(String label) throws XMLStreamException {
    writeElement("label", label);
  }

  protected void writeFile(File file) throws XMLStreamException, IOException {
    ensureStartElementWritten();

    try (InputStream inputStream = fileContentLoader.resolveContent(file.getContent())) {
      writeFile(
          file.getEncodingFormat(),
          file.getFilename(),
          file.getLanguage(),
          inputStream,
          !file.getContent().isBase64encoded());
    }
  }

  protected void writeFile(
      String encodingFormat,
      String filename,
      String language,
      InputStream inputStream,
      boolean encodeToBase64)
      throws XMLStreamException, IOException {

    xmlStreamWriter.writeStartElement(MEMO, "File");

    writeElement("encodingFormat", encodingFormat);
    writeElement("filename", filename);
    writeElement("language", language);

    if (encodeToBase64) {
      encodeContent(inputStream);
    } else {
      streamContent(inputStream);
    }

    xmlStreamWriter.writeEndElement();
  }

  protected void streamContent(InputStream inputStream) throws IOException, XMLStreamException {

    xmlStreamWriter.writeStartElement(MEMO, "content");
    InputStreamReader isr = new InputStreamReader(inputStream);

    char[] buffer = new char[1];
    while (isr.read(buffer) != -1) {
      xmlStreamWriter.writeRaw(buffer, 0, buffer.length);
    }
    xmlStreamWriter.writeEndElement();
  }

  protected void encodeContent(InputStream inputStream) throws IOException, XMLStreamException {
    xmlStreamWriter.writeStartElement(MEMO, "content");
    xmlStreamWriter.writeRaw("");
    xmlStreamWriter.flush();

    Base64StreamEncoder.encodeAsBase64(inputStream, outputStream);

    xmlStreamWriter.writeEndElement();
  }
}
