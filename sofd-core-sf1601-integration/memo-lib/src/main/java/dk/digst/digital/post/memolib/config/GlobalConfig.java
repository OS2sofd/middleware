package dk.digst.digital.post.memolib.config;

import dk.digst.digital.post.memolib.xml.stax.Stax2FactoryProvider;
import javax.xml.stream.XMLInputFactory;
import org.codehaus.stax2.XMLInputFactory2;

public class GlobalConfig {

  public static final String MEMO_CORE_XSD = "/schemas/MeMo_core.xsd";

  private static boolean skipBase64Binary = false;

  private GlobalConfig() {}

  /**
   * Default: true
   *
   * @see javax.xml.stream.XMLInputFactory#IS_COALESCING
   */
  public static void setCoalescing(boolean coalescing) {
    Stax2FactoryProvider.getXmlInputFactory()
        .setProperty(XMLInputFactory.IS_COALESCING, coalescing);
  }

  /**
   * Default: true
   *
   * @see org.codehaus.stax2.XMLInputFactory2#P_PRESERVE_LOCATION
   */
  public static void setPreserveLocation(boolean preserveLocation) {
    Stax2FactoryProvider.getXmlInputFactory()
        .setProperty(XMLInputFactory2.P_PRESERVE_LOCATION, preserveLocation);
  }

  /**
   * @see org.codehaus.stax2.XMLInputFactory2#configureForLowMemUsage()
   */
  public static void configureForLowMemUsage() {
    Stax2FactoryProvider.getXmlInputFactory().configureForLowMemUsage();
  }

  /**
   * @see org.codehaus.stax2.XMLInputFactory2#configureForSpeed()
   */
  public static void configureForSpeed() {
    Stax2FactoryProvider.getXmlInputFactory().configureForSpeed();
  }

  /**
   * Default: false Parser that skips base64Binary for performance reasons. This will not provide
   * proper validation but can be useful in some scenarios, if base64Binary is validated elsewhere.
   */
  public static void setSkipBase64Binary(boolean skipBase64Binary) {
    GlobalConfig.skipBase64Binary = skipBase64Binary;
  }

  public static boolean isSkipBase64Binary() {
    return GlobalConfig.skipBase64Binary;
  }
}
