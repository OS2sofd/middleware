package dk.digst.digital.post.memolib.xml.schema;

import com.ctc.wstx.exc.WstxIOException;
import com.ctc.wstx.msv.W3CSchemaFactory;
import dk.digst.digital.post.memolib.config.GlobalConfig;
import dk.digst.digital.post.memolib.xml.stax.Stax2FactoryProvider;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import javax.xml.stream.XMLStreamException;
import lombok.experimental.UtilityClass;
import org.codehaus.stax2.validation.XMLValidationSchema;
import org.codehaus.stax2.validation.XMLValidationSchemaFactory;
import org.xml.sax.InputSource;

import static org.codehaus.stax2.validation.XMLValidationSchema.SCHEMA_ID_W3C_SCHEMA;

@UtilityClass
public class MeMoValidationSchemaProvider {

  private static XMLValidationSchema xmlValidationSchema;

  private static XMLValidationSchema xmlValidationSchemaSkipBase64Binary;

  /**
   * Provides an XML schema for MeMo message validation
   *
   * @return XMLValidationSchema
   */
  public static XMLValidationSchema getMeMoValidationSchema() throws XMLStreamException {

    if (GlobalConfig.isSkipBase64Binary()) {
      return getXmlValidationSchemaSkipBase64Binary();
    } else {
      return getXmlValidationSchema();
    }
  }

  private XMLValidationSchema getXmlValidationSchema() throws XMLStreamException {
    if (xmlValidationSchema == null) {
      W3CSchemaFactory factory =
          (W3CSchemaFactory) XMLValidationSchemaFactory.newInstance(SCHEMA_ID_W3C_SCHEMA);
      xmlValidationSchema = getXmlSchema(factory);
    }
    return xmlValidationSchema;
  }

  private XMLValidationSchema getXmlValidationSchemaSkipBase64Binary() throws XMLStreamException {

    if (xmlValidationSchemaSkipBase64Binary == null) {
      xmlValidationSchemaSkipBase64Binary = getXmlSchema(new W3CSkipBase64SchemaFactory());
    }
    return xmlValidationSchemaSkipBase64Binary;
  }

  private XMLValidationSchema getXmlSchema(W3CSchemaFactory factory) throws XMLStreamException {
    URL url = Stax2FactoryProvider.class.getResource(GlobalConfig.MEMO_CORE_XSD);
    return factory.createSchema(url);
  }

  private static class W3CSkipBase64SchemaFactory extends W3CSchemaFactory {
    public XMLValidationSchema createSchema(URL url) throws XMLStreamException {
      try {
        String xsd =
            new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        String skipBase64Xsd = xsd.replace("xs:base64Binary", "xs:anyType");
        InputSource src =
            new InputSource(
                new ByteArrayInputStream(skipBase64Xsd.getBytes(StandardCharsets.UTF_8)));
        src.setSystemId(url.toExternalForm());
        return loadSchema(src, url);
      } catch (IOException ioe) {
        throw new WstxIOException(ioe);
      }
    }
  }
}
