package dk.digst.digital.post.memolib.xml.parser;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import dk.digst.digital.post.memolib.model.MeMoClass;
import dk.digst.digital.post.memolib.model.MeMoMessageClassDictionary;
import dk.digst.digital.post.memolib.model.Message;
import dk.digst.digital.post.memolib.parser.MeMoParseException;
import dk.digst.digital.post.memolib.parser.MeMoParser;
import dk.digst.digital.post.memolib.xml.stax.XMLStreamReaderFactory;
import dk.digst.digital.post.memolib.xml.visitor.MeMoStreamVisitor;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.stream.XMLStreamException;
import org.codehaus.stax2.XMLStreamReader2;

/**
 * The MeMoXmlStreamParser can be used to parse a MeMo. It uses StAX to parse the XML document and
 * Jackson for data-binding. If validation is enabled the MeMo will be validated while parsing the
 * content.
 */
public class MeMoXmlStreamParser implements MeMoParser {
  private boolean xmlStreamOpen;
  private final XMLStreamReader2 xmlStream;
  private final XmlMapper xmlMapper;

  MeMoXmlStreamParser(
      XmlMapper xmlMapper, InputStream inputStream, XMLStreamReaderFactory xmlStreamReaderFactory)
      throws XMLStreamException {
    this.xmlMapper = xmlMapper;
    this.xmlStream = xmlStreamReaderFactory.createXMLStreamReader(inputStream);
    this.xmlStreamOpen = true;
  }

  @Override
  public Message parse() throws MeMoParseException, IOException {
    try {
      xmlStream.nextTag();
      if (!xmlStream.getLocalName().equalsIgnoreCase("Message")) {
        throw new MeMoParseException("unexpected element: " + xmlStream.getLocalName());
      }
      return xmlMapper.readValue(xmlStream, Message.class);
    } catch (XMLStreamException e) {
      throw new MeMoParseException(e);
    } finally {
      closeXmlStream();
    }
  }

  @Override
  public void traverse(MeMoStreamVisitor... visitors) throws MeMoParseException, IOException {

    MeMoXmlStreamLocation location = new MeMoXmlStreamLocation();

    try {
      while (nextStartElement()) {
        boolean cursorMoved = handleElement(location, visitors);
        nextElementIf(!cursorMoved);
      }
    } catch (XMLStreamException e) {
      throw new MeMoParseException(e);
    } finally {
      closeXmlStream();
    }
  }

  /**
   * A method to close the xml stream. Should be called after parsing the message.
   */
  @Override
  public void close() throws IOException {
    closeXmlStream();
  }

  @Override
  public boolean isClosed() {
    return !xmlStreamOpen;
  }

  public void traverseChildren(
      MeMoXmlStreamLocation location, int parentDepth, MeMoStreamVisitor... visitors)
      throws XMLStreamException, IOException {

    while (nextStartElement()) {
      // make sure that we are still in the scope of the element
      if (this.xmlStream.getDepth() <= parentDepth) {
        break;
      }

      boolean cursorMoved = handleElement(location, visitors);
      nextElementIf(!cursorMoved);
    }
  }

  private boolean handleElement(MeMoXmlStreamLocation location, MeMoStreamVisitor[] visitors)
      throws IOException, XMLStreamException {
    String elementName = this.xmlStream.getLocalName();
    MeMoXmlStreamCursor cursor = new MeMoXmlStreamCursor(elementName, location, this);

    if (MeMoMessageClassDictionary.isMeMoClass(elementName)) {
      updateLocation(location, elementName);

      // let each visitor visit the cursor
      for (MeMoStreamVisitor visitor : visitors) {
        cursor.accept(visitor);
      }
    }

    return cursor.getState() != MeMoXmlStreamCursor.CursorState.INITIAL_POSITION;
  }

  private void updateLocation(MeMoXmlStreamLocation location, String elementName) {
    Class<? extends MeMoClass> meMoModelClass =
        MeMoMessageClassDictionary.getMeMoClass(elementName);
    location.updateStack(this.xmlStream.getDepth(), meMoModelClass);
  }

  private void nextElementIf(boolean isTrue) throws XMLStreamException {
    if (isTrue && xmlStream.hasNext()) {
      xmlStream.next();
    }
  }

  private void nextElement() throws XMLStreamException {
    if (xmlStream.hasNext()) {
      xmlStream.next();
    }
  }

  private boolean nextStartElement() throws XMLStreamException {
    while (xmlStream.hasNext() && !xmlStream.isStartElement()) {
      xmlStream.next();
    }

    return xmlStream.hasNext();
  }

  <T> Object parseClass(Class<T> sourceClazz) throws XMLStreamException {
    if (!this.xmlStream.isStartElement()) {
      throw new XMLStreamException("Parser must be on START_ELEMENT to read next text");
    }

    try {
      return xmlMapper.readValue(this.xmlStream, sourceClazz);
    } catch (IOException e) {
      throw new XMLStreamException(e);
    }
  }

  MeMoClassElements parseElements() throws XMLStreamException {
    MeMoClassElements meMoClassElements = new MeMoClassElements(xmlMapper);

    String xmlElementName = this.xmlStream.getLocalName();
    if (!MeMoMessageClassDictionary.isMeMoClass(xmlElementName)) {
      throw new IllegalStateException(
          String.format("Not able to parse elements for %s", xmlElementName));
    }

    nextElement();

    while (nextStartElement()) {
      String currentXmlElement = this.xmlStream.getLocalName();

      if (MeMoMessageClassDictionary.isMeMoClass(currentXmlElement)) {
        break;
      }
      meMoClassElements.addElement(currentXmlElement, this.xmlStream.getElementText());
    }
    return meMoClassElements;
  }

  /**
   * This method does not close the underlying input stream.
   */
  private void closeXmlStream() throws IOException {
    try {
      xmlStream.close();
      xmlStreamOpen = false;
    } catch (XMLStreamException e) {
      throw new IOException(e);
    }
  }
}
