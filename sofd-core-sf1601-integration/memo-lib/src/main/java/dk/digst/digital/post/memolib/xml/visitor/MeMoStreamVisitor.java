package dk.digst.digital.post.memolib.xml.visitor;

import dk.digst.digital.post.memolib.xml.parser.MeMoXmlStreamCursor;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;

/**
 * Represents a visitor of a streamed MeMo message during deserialization. The visitor can access
 * the underlying StAX {@link javax.xml.stream.XMLStreamReader} by a {@link MeMoXmlStreamCursor} and
 * apply logic to influence how the MeMo message is handled.
 *
 */
public interface MeMoStreamVisitor {

  /**
   * This method is invoked when the xml stream reaches an xml element which represents a
   * MeMo class (MainDocument, File, etc.)
   *
   * @param meMoXmlStreamCursor a cursor which represents the current position in the underlying stream
   * @throws XMLStreamException if a problem occurs while reading and parsing the stream
   * @throws IOException if a low-level I/O problem occurs
   */
  void visitMeMoClass(MeMoXmlStreamCursor meMoXmlStreamCursor) throws XMLStreamException, IOException;
}
