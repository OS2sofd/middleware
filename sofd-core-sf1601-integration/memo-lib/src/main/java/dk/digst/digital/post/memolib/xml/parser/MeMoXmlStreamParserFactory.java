package dk.digst.digital.post.memolib.xml.parser;

import dk.digst.digital.post.memolib.parser.MeMoParser;
import dk.digst.digital.post.memolib.xml.stax.Stax2FactoryProvider;
import dk.digst.digital.post.memolib.xml.stax.XMLStreamReaderFactory;
import java.io.InputStream;
import javax.xml.stream.XMLStreamException;
import lombok.experimental.UtilityClass;

import static dk.digst.digital.post.memolib.xml.mapper.ObjectMapperProvider.getXmlMapper;

/**
 * The MeMoParserFactory is used to create a parser which can parse an XML representation of a MeMo
 * message. It handles the configuration of the Jackson XmlMapper and StAX.
 */
@UtilityClass
public class MeMoXmlStreamParserFactory {

  /**
   * A method to create a basic non-validating parser
   *
   * @param inputStream a XML-specific input stream
   * @return a streaming MeMo message parser
   * @throws XMLStreamException if a problem occur when creating the xml stream
   */
  public MeMoParser createParser(InputStream inputStream) throws XMLStreamException {
    return createParser(inputStream, false);
  }

  /**
   * A method which can be used to create a validating parser. In contrast to the basic {@link
   * #createParser} method, this method takes a boolean flag to indicate if the MeMo XML schema file
   * must be used by the {@link MeMoParser} to validate the XML document while parsing the input.
   *
   * @param inputStream a XML-specific input stream
   * @param enableValidation if true the MeMo is validated
   * @return a streaming MeMo message parser
   * @throws XMLStreamException if a problem occur when creating the xml stream
   */
  public MeMoParser createParser(InputStream inputStream, boolean enableValidation)
          throws XMLStreamException {

    XMLStreamReaderFactory xmlStreamReaderFactory =
            Stax2FactoryProvider.createXMLStreamReaderFactory(enableValidation);

    return new MeMoXmlStreamParser(getXmlMapper(), inputStream, xmlStreamReaderFactory);
  }


}
