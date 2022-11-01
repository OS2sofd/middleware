package dk.digst.digital.post.memolib.xml.parser;

import dk.digst.digital.post.memolib.model.MeMoClass;
import dk.digst.digital.post.memolib.xml.visitor.MeMoStreamVisitor;
import java.io.IOException;
import java.util.Optional;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/** This class exposes a limited view of the state of {@link XMLStreamReader} and
 * provided a limited API to parse the elements of the xml stream. */
public class MeMoXmlStreamCursor {

  public enum CursorState {
    INITIAL_POSITION,
    ELEMENTS_PARSED,
    PARSED
  }

  private final String xmlElementName;
  private final MeMoXmlStreamLocation location;
  private final MeMoXmlStreamParser parser;
  private CursorState state;

  private Object parsedObject;
  private MeMoClassElements parsedMeMoClassElements;

  public MeMoXmlStreamCursor(
          String xmlElementName, MeMoXmlStreamLocation location, MeMoXmlStreamParser parser) {
    this.xmlElementName = xmlElementName;
    this.location = location;
    this.parser = parser;
    this.state = CursorState.INITIAL_POSITION;
  }

  /**
   * Returns the local name of the xml element
   *
   * @return xml element name
   */
  public String getXmlElementName() {
    return xmlElementName;
  }

  /**
   * Returns the MeMo class which is currently in context. The actual xml stream cursor
   * might point to a simple element/field within the context of this MeMo class
   *
   * @return MeMo class
   */
  public Class<? extends MeMoClass> getCurrentPosition() {
    return location.getCurrentPosition();
  }

  /**
   * returns the parent MeMo class of the current one.
   *
   * @return MeMo class
   */
  public Optional<Class<? extends MeMoClass>> getParentPosition() {
    return location.getParentPosition();
  }

  /**
   * returns the state of the {@link MeMoXmlStreamCursor} which is used to
   * determine if the state of the underlying xml stream has been changed
   * by invoking methods in this class.
   *
   * @return the state
   */
  public CursorState getState() {
    return state;
  }

  /**
   * Parses the MeMo class and returns a complete object. The CursorState is changed
   * due to this, preventing later invocations of the methods traverseChildren or parseMeMoClassElements
   * which is no longer possible.
   *
   * @param clazz The type to which the xml should be parsed
   * @return a MeMo class object
   * @throws XMLStreamException
   */
  @SuppressWarnings("unchecked")
  public <T> T parseMeMoClass(Class<T> clazz) throws XMLStreamException {
    if (parsedObject != null) {
      return (T) parsedObject;
    }

    if (state != CursorState.INITIAL_POSITION) {
      throw new IllegalStateException(
          "MeMo Class can't be parsed. parseElements has already been called");
    }

    parsedObject = parser.parseClass(location.getCurrentPosition());
    state = CursorState.PARSED;

    return (T) parsedObject;
  }

  /**
   * Parses the elements (fields) of the MeMo class. Child MeMo classes are not parsed
   * and can be handled by another visitor or by invoking the method traverseChildren.
   *
   * The returned object wraps the elements.
   *
   * @return MeMoClassElements
   * @throws XMLStreamException
   */
  public MeMoClassElements parseMeMoClassElements() throws XMLStreamException {
    if (parsedMeMoClassElements != null) {
      return parsedMeMoClassElements;
    }

    if (state != CursorState.INITIAL_POSITION) {
      throw new IllegalStateException(
          "MeMo Class elements can't be parsed. parseMeMoClass has already been called");
    }

    parsedMeMoClassElements = parser.parseElements();
    state = CursorState.ELEMENTS_PARSED;

    return parsedMeMoClassElements;
  }

  /**
   * traverses the child MeMo classes with a list of visitors. If the method parseMeMoClass has already
   * been invoked, this methods throws an {@link IllegalStateException}
   *
   * @param visitors
   * @throws IOException
   * @throws XMLStreamException
   */
  @SafeVarargs
  public final void traverseChildren(MeMoStreamVisitor... visitors)
      throws IOException, XMLStreamException {

    if (state == CursorState.PARSED) {
      throw new IllegalStateException(
          "Children can't be traversed. parseMeMoClass has already been called");
    }

    parser.traverseChildren(new MeMoXmlStreamLocation(location), location.getDepth(), visitors);
  }

  /**
   * Dispatches to the visitor.
   *
   * @param visitor
   * @throws IOException
   * @throws XMLStreamException
   */
  public void accept(MeMoStreamVisitor visitor)
      throws IOException, XMLStreamException {
    visitor.visitMeMoClass(this);
  }
}
