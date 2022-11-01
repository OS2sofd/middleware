package dk.digst.digital.post.memolib.xml.writer;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import dk.digst.digital.post.memolib.model.MemoVersion;
import dk.digst.digital.post.memolib.model.Message;
import dk.digst.digital.post.memolib.model.MessageBody;
import dk.digst.digital.post.memolib.model.MessageHeader;
import dk.digst.digital.post.memolib.writer.FileContentLoader;
import dk.digst.digital.post.memolib.writer.MeMoStreamWriter;
import dk.digst.digital.post.memolib.writer.MeMoWriteException;
import java.io.IOException;
import java.io.OutputStream;
import javax.xml.stream.XMLStreamException;
import lombok.NonNull;
import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamWriter2;
import org.codehaus.stax2.validation.XMLValidationSchema;

import static dk.digst.digital.post.memolib.model.Namespace.DMV;
import static dk.digst.digital.post.memolib.model.Namespace.FORM;
import static dk.digst.digital.post.memolib.model.Namespace.GLN;
import static dk.digst.digital.post.memolib.model.Namespace.GRD;
import static dk.digst.digital.post.memolib.model.Namespace.KLE;
import static dk.digst.digital.post.memolib.model.Namespace.MEMO;
import static dk.digst.digital.post.memolib.model.Namespace.SOR;
import static dk.digst.digital.post.memolib.model.Namespace.UDD;

/**
 * MeMoStreamXmlWriter supports writing a MeMo message as a stream by using an underlying StAX
 * {@link javax.xml.stream.XMLStreamWriter}.
 */
public class MeMoStreamWriterImpl implements MeMoStreamWriter {

  private enum InternalState {
    INITIALIZED,
    IN_PROGRESS,
    CLOSED
  }

  private final XMLStreamWriter2 xmlStreamWriter;
  private final XmlMapper xmlMapper;
  private final OutputStream outputStream;
  private final XMLValidationSchema schema;
  private final FileContentLoader fileContentLoader;

  /* indicates that the writer has been closed. Used to ensure that calling the method more than once creates the same result */
  private InternalState state;

  public MeMoStreamWriterImpl(
      OutputStream outputStream,
      XMLOutputFactory2 xmlOutputFactory2,
      XmlMapper xmlMapper,
      XMLValidationSchema schema,
      FileContentLoader fileContentLoader)
      throws XMLStreamException {

    this.xmlStreamWriter = (XMLStreamWriter2) xmlOutputFactory2.createXMLStreamWriter(outputStream);
    this.xmlMapper = xmlMapper;
    this.outputStream = outputStream;
    this.schema = schema;
    this.fileContentLoader = fileContentLoader;
    state = InternalState.INITIALIZED;
  }

  @Override
  public void write(Message message) throws MeMoWriteException, IOException {

    try {
      writeStartOfMessage().writeHeader(message.getMessageHeader());

      if (message.getMessageBody() != null) {
        writeBody(message.getMessageBody());
      }
    } catch (XMLStreamException e) {
      throw new MeMoWriteException(e);
    } catch (JsonGenerationException e) {
      throw new MeMoWriteException(e);
    } catch (JsonMappingException e) {
      throw new MeMoWriteException(e);
    }
  }

  /**
   * This method writes the start of the XML document to the stream and adds the required namespace
   * definitions.
   *
   * @return this
   * @throws XMLStreamException if a problem occur when writing to the xml stream
   */
  public MeMoStreamWriterImpl writeStartOfMessage() throws XMLStreamException {
    xmlStreamWriter.writeStartDocument();
    xmlStreamWriter.writeStartElement("memo", "Message", MEMO);

    if (schema != null) {
      xmlStreamWriter.validateAgainst(schema);
    }
    xmlStreamWriter.writeAttribute("memoVersion", MemoVersion.MEMO_VERSION.toString());
    xmlStreamWriter.writeAttribute("memoSchVersion", MemoVersion.MEMO_SCH_VERSION);
    xmlStreamWriter.writeNamespace("memo", MEMO);
    xmlStreamWriter.writeNamespace("grd", GRD);
    xmlStreamWriter.writeNamespace("gln", GLN);
    xmlStreamWriter.writeNamespace("udd", UDD);
    xmlStreamWriter.writeNamespace("form", FORM);
    xmlStreamWriter.writeNamespace("dmv", DMV);
    xmlStreamWriter.writeNamespace("kle", KLE);
    xmlStreamWriter.writeNamespace("sor", SOR);

    state = InternalState.IN_PROGRESS;

    return this;
  }

  /**
   * This method writes the {@link MessageHeader} to the xml stream.
   *
   * @param messageHeader the header to be written
   * @return this
   * @throws IOException if a low-level I/O problem occurs
   */
  public MeMoStreamWriterImpl writeHeader(MessageHeader messageHeader) throws IOException {
    xmlMapper.writeValue(xmlStreamWriter, messageHeader);
    return this;
  }

  /**
   * This method writes the {@link MessageBody} to the xml stream.
   *
   * @param messageBody the message body to be written
   * @return this
   * @throws IOException if a low-level I/O problem occurs
   * @throws XMLStreamException if a problem occurs when writing to the xml stream
   */
  public MeMoStreamWriterImpl writeBody(@NonNull MessageBody messageBody)
      throws IOException, XMLStreamException {

    body().write(messageBody);
    return this;
  }

  @Override
  public void close() throws IOException {
    if (isClosed()) return;

    try {
      xmlStreamWriter.close();
    } catch (XMLStreamException e) {
      if (state.equals(InternalState.INITIALIZED)) {
        throw new IOException(e);
      }
    } finally {
      state = InternalState.CLOSED;
    }
  }

  /**
   * This method closes both the {@link javax.xml.stream.XMLStreamWriter} and the underlying
   * outputStream.
   *
   * @throws IOException if a low-level I/O problem occurs
   */
  @Override
  public void closeStream() throws IOException {
    if (!isClosed()) {
      close();
    }

    outputStream.close();
  }

  /**
   * This method can be used to determine if the {@link javax.xml.stream.XMLStreamWriter} is closed.
   *
   * @return a boolean indicating if the {@link javax.xml.stream.XMLStreamWriter} is closed.
   */
  @Override
  public boolean isClosed() {
    return state == InternalState.CLOSED;
  }

  @Override
  public FileContentLoader getFileContentLoader() {
    return fileContentLoader;
  }

  private MessageBodyWriter body() {
    return new MessageBodyWriter(xmlStreamWriter, xmlMapper, fileContentLoader, outputStream);
  }
}
