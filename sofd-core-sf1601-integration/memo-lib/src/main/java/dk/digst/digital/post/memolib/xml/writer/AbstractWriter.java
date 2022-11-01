package dk.digst.digital.post.memolib.xml.writer;

import dk.digst.digital.post.memolib.model.Namespace;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import javax.xml.stream.XMLStreamException;
import lombok.NonNull;
import org.apache.commons.text.StringEscapeUtils;
import org.codehaus.stax2.XMLStreamWriter2;

import static dk.digst.digital.post.memolib.model.Namespace.MEMO;
import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;

/** Abstract class containing common functionality for XMLStreamWriters */
public abstract class AbstractWriter {
  private final String elementName;
  protected boolean isStartElementWritten = false;
  protected final XMLStreamWriter2 xmlStreamWriter;

  public AbstractWriter(XMLStreamWriter2 xmlStreamWriter, String elementName) {
    this.xmlStreamWriter = xmlStreamWriter;
    this.elementName = elementName;
  }

  /**
   * Writes an element of type LocalDateTime. The element will be in the MeMo namespace.
   *
   * @param elementName the local name of the element
   * @param value the value of the element
   * @throws XMLStreamException if a problem occurs when writing to the xml stream
   */
  public void writeElement(@NonNull String elementName, LocalDateTime value)
      throws XMLStreamException {
    if (value != null) {
      /* We must convert the local dateTime to ensure that the time is represented correctly
      with the UTC offset in the serialized XML message */
      OffsetDateTime utcDateTime = OffsetDateTime.of(value, ZoneOffset.UTC).withNano(0);
      writeElement(elementName, utcDateTime.format(ISO_ZONED_DATE_TIME));
    }
  }

  /**
   * Writes an element of type LocalDate. The element will be in the MeMo namespace.
   *
   * @param elementName the local name of the element
   * @param value the value of the element
   * @throws XMLStreamException if a problem occurs when writing to the xml stream
   */
  public void writeElement(@NonNull String elementName, LocalDate value) throws XMLStreamException {
    if (value != null) {
      writeElement(elementName, value.toString());
    }
  }

  /**
   * Writes an element of type Integer. The element will be in the MeMo namespace.
   *
   * @param elementName the local name of the element
   * @param value the value of the element
   * @throws XMLStreamException if a problem occurs when writing to the xml stream
   */
  public void writeElement(@NonNull String elementName, Integer value) throws XMLStreamException {
    if (value != null) {
      writeElement(elementName, value.toString());
    }
  }

  /**
   * Writes an element of type Boolean. The element will be in the MeMo namespace.
   *
   * @param elementName the local name of the element
   * @param value the value of the element
   * @throws XMLStreamException if a problem occurs when writing to the xml stream
   */
  public void writeElement(@NonNull String elementName, Boolean value) throws XMLStreamException {
    if (value != null) {
      writeElement(elementName, value.toString());
    }
  }

  /**
   * Writes an element of type String. The element will be in the MeMo namespace.
   *
   * @param elementName the local name of the element
   * @param value the value of the element
   * @throws XMLStreamException if a problem occurs when writing to the xml stream
   */
  public void writeElement(@NonNull String elementName, String value) throws XMLStreamException {
    if (value != null) {
      xmlStreamWriter.writeStartElement(Namespace.MEMO, elementName);
      xmlStreamWriter.writeRaw(StringEscapeUtils.escapeXml10(value));
      xmlStreamWriter.writeEndElement();
    }
  }

  /**
   * This method must be called to ensure that the start element will be written before writing any
   * child element.
   *
   * @throws XMLStreamException if a problem occurs when writing to the xml stream
   */
  protected void ensureStartElementWritten() throws XMLStreamException {
    if (!isStartElementWritten) {
      xmlStreamWriter.writeStartElement(MEMO, elementName);
      isStartElementWritten = true;
    }
  }
}
