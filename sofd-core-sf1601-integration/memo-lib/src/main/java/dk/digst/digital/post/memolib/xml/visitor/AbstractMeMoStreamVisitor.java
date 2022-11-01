package dk.digst.digital.post.memolib.xml.visitor;

import dk.digst.digital.post.memolib.model.MeMoClass;
import dk.digst.digital.post.memolib.xml.parser.MeMoXmlStreamCursor;
import java.util.Optional;

/**
 * An abstract implementation of {@link MeMoStreamVisitor} which contains common methods used in
 * subclasses.
 *
 * @param <T> Type of MeMoClass
 */
abstract class AbstractMeMoStreamVisitor<T> implements MeMoStreamVisitor {
  protected final Class<T> clazz;
  protected final Class<? extends MeMoClass> parentClazz;

  protected AbstractMeMoStreamVisitor(Class<T> clazz, Class<? extends MeMoClass> parentClazz) {
    this.clazz = clazz;
    this.parentClazz = parentClazz;
  }

  protected boolean isRequiredParentMeMoClass(MeMoXmlStreamCursor meMoXmlStreamCursor) {
    Optional<Class<? extends MeMoClass>> parent = meMoXmlStreamCursor.getParentPosition();
    return parent.isPresent() && parentClazz.isAssignableFrom(parent.get());
  }

  protected boolean isRequiredMeMoClass(MeMoXmlStreamCursor meMoXmlStreamCursor) {
    return clazz.isAssignableFrom(meMoXmlStreamCursor.getCurrentPosition());
  }
}
