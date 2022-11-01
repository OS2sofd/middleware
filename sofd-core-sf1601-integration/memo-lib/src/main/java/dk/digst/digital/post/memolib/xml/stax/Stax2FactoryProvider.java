package dk.digst.digital.post.memolib.xml.stax;

import com.ctc.wstx.stax.WstxOutputFactory;
import dk.digst.digital.post.memolib.xml.schema.MeMoValidationSchemaProvider;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.validation.XMLValidationSchema;

/**
 * This class provides StAX2 factories ({@link XMLInputFactory2}, {@link XMLOutputFactory2}) which
 * are configured to handle MeMo messages.
 */
public class Stax2FactoryProvider {

  private static XMLInputFactory2 xmlInputFactory;
  private static XMLOutputFactory2 xmlOutputFactory;

  private Stax2FactoryProvider() {}

  /**
   * Provides a {@link XMLInputFactory2} which is configured to prevent XML eXternal Entity
   * injection (XXE). The following properties are set to false:
   *
   * <p>XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES
   * <p>XMLInputFactory.SUPPORT_DTD
   *
   * <p>To handle character elements consistently, text coalescing has been enabled by setting the
   * following property to true:
   *
   * <p>XMLInputFactory.IS_COALESCING
   *
   * @return a XMLInputFactory2
   */
  public static XMLInputFactory2 getXmlInputFactory() {
    if (xmlInputFactory == null) {
      XMLInputFactory2 factory = (XMLInputFactory2) XMLInputFactory.newFactory();

      /* prevent XML eXternal Entity injection (XXE) */
      factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
      factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
      factory.setProperty(XMLInputFactory.IS_COALESCING, true);
      xmlInputFactory = factory;
    }

    return xmlInputFactory;
  }

  /**
   * Provides a {@link XMLOutputFactory2} which is configured so that namespace prefixes are added
   * to all elements in the MeMo xml document.
   *
   * @return a XMLOutputFactory2
   */
  public static XMLOutputFactory2 getXmlOutputFactory() {
    if (xmlOutputFactory == null) {
      WstxOutputFactory factory = (WstxOutputFactory) XMLOutputFactory.newFactory();
      /* This setting is required so that Jackson will add namespace prefixes when using the mapper */
      factory.getConfig().enableAutomaticNamespaces(true);

      xmlOutputFactory = factory;
    }

    return xmlOutputFactory;
  }

  /**
   * Provides a basic {@link XMLStreamReaderFactory} which can create instances of {@link
   * XMLStreamReader2} without XML Schema validation.
   *
   * @return a XMLStreamReaderFactory
   */
  public static XMLStreamReaderFactory createXMLStreamReaderFactory() {
    return new XMLStreamReaderFactory(getXmlInputFactory(), null);
  }

  /**
   * Provides a {@link XMLStreamReaderFactory} which can create instances of {@link
   * XMLStreamReader2} with XML Schema validation.
   *
   * @param enableValidation if true the MeMo XML Schema is used to validate the read file
   * @return a XMLStreamReaderFactory
   * @throws XMLStreamException if an underlying xml stream exception occurs
   */
  public static XMLStreamReaderFactory createXMLStreamReaderFactory(boolean enableValidation)
      throws XMLStreamException {

    XMLValidationSchema schema = null;

    if (enableValidation) {
      schema = MeMoValidationSchemaProvider.getMeMoValidationSchema();
    }

    return new XMLStreamReaderFactory(getXmlInputFactory(), schema);
  }
}
