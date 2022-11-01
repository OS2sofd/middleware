package dk.digst.digital.post.memolib.xml.stax;

import java.io.InputStream;
import javax.xml.stream.XMLStreamException;
import lombok.NonNull;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.validation.XMLValidationSchema;

/**
 * The XMLStreamReaderFactory can be used to create instances of a {@link XMLStreamReader2} from a
 * {@link InputStream}. If a {@link XMLValidationSchema} is provided when the factory is
 * constructed, it will create instances which will do XML Schema validation when reading the
 * stream.
 */
public class XMLStreamReaderFactory {

  private final XMLInputFactory2 xmlInputFactory;
  private final XMLValidationSchema schema;

  XMLStreamReaderFactory(@NonNull XMLInputFactory2 xmlInputFactory, XMLValidationSchema schema) {
    this.xmlInputFactory = xmlInputFactory;
    this.schema = schema;
  }

  /**
   * Creates an instance of {@link XMLStreamReader2} based on a {@link InputStream}.
   *
   * @param inputStream the input stream
   * @return a XMLStreamReader2
   * @throws XMLStreamException if the provided schema is invalid
   */
  public XMLStreamReader2 createXMLStreamReader(InputStream inputStream) throws XMLStreamException {
    XMLStreamReader2 xmlStream =
        (XMLStreamReader2) xmlInputFactory.createXMLStreamReader(inputStream);

    if (schema != null) {
      xmlStream.validateAgainst(schema);
    }

    return xmlStream;
  }
}
