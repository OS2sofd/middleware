package dk.digst.digital.post.memolib.xml.visitor;

import dk.digst.digital.post.memolib.model.MeMoClass;
import dk.digst.digital.post.memolib.util.XmlStreamConsumer;
import dk.digst.digital.post.memolib.xml.parser.MeMoXmlStreamCursor;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;

/**
 * This class is an an implementation of {@link MeMoStreamVisitor} which delegates the handling of
 * the xml stream to a {@link XmlStreamConsumer}
 *
 * @param <T> Type of MeMo class to visit
 */
public class MeMoClassConsumer<T> extends AbstractMeMoStreamVisitor<T> {
  private final XmlStreamConsumer<MeMoXmlStreamCursor> consumer;

  MeMoClassConsumer(
      Class<T> clazz,
      Class<? extends MeMoClass> parentClazz,
      XmlStreamConsumer<MeMoXmlStreamCursor> consumer) {
    super(clazz, parentClazz);
    this.consumer = consumer;
  }

  /**
   * This method invokes the consumer if the {@link MeMoXmlStreamCursor} position is at the expected MeMo class.
   *
   * @param meMoXmlStreamCursor a cursor which represents the current position in the underlying stream
   * @throws IOException
   * @throws XMLStreamException
   */
  @Override
  public void visitMeMoClass(MeMoXmlStreamCursor meMoXmlStreamCursor)
      throws IOException, XMLStreamException {

    if (isRequiredMeMoClass(meMoXmlStreamCursor)
        && (parentClazz == null || isRequiredParentMeMoClass(meMoXmlStreamCursor))) {
      consumer.accept(meMoXmlStreamCursor);
    }
  }
}
