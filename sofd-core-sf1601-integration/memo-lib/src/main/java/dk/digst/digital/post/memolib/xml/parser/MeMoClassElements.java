package dk.digst.digital.post.memolib.xml.parser;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A wrapper for MeMo class elements (fields). Provides convenience methods to convert fields which
 * has a datatype other than Text.
 */
public class MeMoClassElements {
  private final XmlMapper xmlMapper;
  private final Map<String, String> elements = new HashMap<>();

  public MeMoClassElements(XmlMapper xmlMapper) {
    this.xmlMapper = xmlMapper;
  }

  /**
   * Adds an element
   *
   * @param elementName
   * @param elementText
   */
  public void addElement(String elementName, String elementText) {
    elements.put(elementName, elementText);
  }

  /**
   * Returns the text value of element if present
   *
   * @param elementName
   * @return an Optional of the element text value
   */
  public Optional<String> get(String elementName) {
    return Optional.ofNullable(elements.get(elementName));
  }

  /**
   * Returns an element value if present. The value is converted to the type specified.
   *
   * @param elementName
   * @param clazz the class to which the value should be converted
   * @return an Optional of the element value
   */
  public <T> Optional<T> get(String elementName, Class<T> clazz) {
    return get(elementName).map(elementText -> xmlMapper.convertValue(elementText, clazz));
  }
}
