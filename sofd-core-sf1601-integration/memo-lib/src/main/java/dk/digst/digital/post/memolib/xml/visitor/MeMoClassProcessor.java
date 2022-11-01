package dk.digst.digital.post.memolib.xml.visitor;

import dk.digst.digital.post.memolib.model.MeMoClass;
import dk.digst.digital.post.memolib.xml.parser.MeMoXmlStreamCursor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.xml.stream.XMLStreamException;

/**
 * This class is an an implementation of {@link MeMoStreamVisitor} which parses and collects MeMo
 * class objects. It implements {@link MeMoStreamProcessorVisitor} which exposes methods to get the
 * result.
 *
 * @param <T> Type of MeMo class to visit
 */
public class MeMoClassProcessor<T> extends AbstractMeMoStreamVisitor<T>
    implements MeMoStreamProcessorVisitor<T> {
  private final List<T> collectedObjects = new ArrayList<>();

  MeMoClassProcessor(Class<T> clazz, Class<? extends MeMoClass> parentClazz) {
    super(clazz, parentClazz);
  }

  /**
   * This method invokes the consumer if the {@link MeMoXmlStreamCursor} position is at the expected
   * MeMo class.
   *
   * @param meMoXmlStreamCursor a cursor which represents the current position in the underlying
   *     stream
   * @throws XMLStreamException
   */
  @Override
  public void visitMeMoClass(MeMoXmlStreamCursor meMoXmlStreamCursor) throws XMLStreamException {
    if (isRequiredMeMoClass(meMoXmlStreamCursor)
        && (parentClazz == null || isRequiredParentMeMoClass(meMoXmlStreamCursor))) {

      T parsedObject = meMoXmlStreamCursor.parseMeMoClass(clazz);
      collectedObjects.add(parsedObject);
    }
  }

  @Override
  public List<T> getResult() {
    return collectedObjects;
  }

  @Override
  public Optional<T> getSingleResult() {
    if (collectedObjects.size() > 1) {
      throw new IllegalStateException(
          String.format("size of collected objects is %s", collectedObjects.size()));
    } else if (collectedObjects.isEmpty()) {
      return Optional.empty();
    } else {
      return Optional.of(collectedObjects.get(0));
    }
  }
}
