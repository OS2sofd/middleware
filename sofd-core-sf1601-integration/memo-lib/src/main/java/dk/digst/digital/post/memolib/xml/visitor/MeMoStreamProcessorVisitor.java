package dk.digst.digital.post.memolib.xml.visitor;

import java.util.List;
import java.util.Optional;

/**
 * Represents a visitor which collects values/objects while visiting the xml stream. *
 *
 * @param <T> Type of Object to collect
 */
public interface MeMoStreamProcessorVisitor<T> extends MeMoStreamVisitor {

  /**
   * Returns the collected objects
   *
   * @return a list of collected objects
   */
  List<T> getResult();

  /**
   * Returns a single result. If more objects has been collected, the method should throw an
   * exception.
   *
   * @return an Optional of the collected object
   */
  Optional<T> getSingleResult();
}
