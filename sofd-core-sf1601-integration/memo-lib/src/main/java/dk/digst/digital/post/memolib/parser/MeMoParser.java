package dk.digst.digital.post.memolib.parser;

import dk.digst.digital.post.memolib.model.Message;
import dk.digst.digital.post.memolib.xml.parser.MeMoXmlStreamCursor;
import dk.digst.digital.post.memolib.xml.visitor.MeMoStreamVisitor;
import java.io.IOException;

public interface MeMoParser {

  /**
   * Parses the input stream to a {@link Message}.
   *
   * @return a MeMo Message
   * @throws MeMoParseException if a problem occurs while reading and parsing the stream
   * @throws IOException if a low-level I/O problem occurs
   */
  Message parse() throws MeMoParseException, IOException;

  /**
   * This method traverses the entire xml stream and applies the provided {@link MeMoStreamVisitor}
   * visitors for each element which is of type MeMo class. The visitor gains access to the {@link
   * MeMoXmlStreamCursor} which can be used to parse either the child elements or the whole MeMo
   * class and as a side-effect move the underlying StAX cursor. When using this method this should
   * be taken into consideration if more visitors are applied to the same stream.
   *
   * @param visitors a list of {@link MeMoStreamVisitor} which will be applied to the stream
   * @throws MeMoParseException
   * @throws IOException if a low-level I/O problem occurs
   */
  void traverse(MeMoStreamVisitor... visitors) throws MeMoParseException, IOException;

  boolean isClosed();

  void close() throws IOException;
}
