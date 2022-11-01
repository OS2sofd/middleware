package dk.digst.digital.post.memolib.parser;

import dk.digst.digital.post.memolib.xml.parser.MeMoXmlStreamParserFactory;
import java.io.InputStream;
import javax.xml.stream.XMLStreamException;

/**
 * The MeMoParserFactory is used to create a parser which can parse a representation of a MeMo
 * message.
 */
public class MeMoParserFactory {

  private MeMoParserFactory() {}

  /**
   * Creates a basic non-validating parser for memo messages
   *
   * @param inputStream input stream containing the memo message
   * @return a memo parser
   */
  public static MeMoParser createParser(InputStream inputStream) throws MeMoParseException {
    return createParser(inputStream, false);
  }

  /**
   * A method which can be used to create a validating parser. In contrast to the basic {@link
   * #createParser} method, this method takes a boolean flag to indicate if validation should be
   * enabled.
   *
   * @param inputStream input stream containing the memo message
   * @return a memo parser
   */
  public static MeMoParser createParser(InputStream inputStream, boolean enableValidation)
      throws MeMoParseException {

    try {
      return MeMoXmlStreamParserFactory.createParser(inputStream, enableValidation);
    } catch (XMLStreamException e) {
      throw new MeMoParseException(e);
    }
  }
}
