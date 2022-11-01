package dk.digst.digital.post.memolib.xml.parser;

import dk.digst.digital.post.memolib.model.MeMoClass;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

/**
 * Represents the location of the xml stream cursor in the MeMo message structure.
 */
public class MeMoXmlStreamLocation {
  private ArrayList<Class<? extends MeMoClass>> elementStack = new ArrayList<>(5);

  public MeMoXmlStreamLocation() {}

  @SafeVarargs
  public MeMoXmlStreamLocation(Class<? extends MeMoClass>... meMoClasses) {
    Collections.addAll(elementStack, meMoClasses);
  }

  /**
   * This method creates a copy of another {@link MeMoXmlStreamLocation}.
   *
   * @param other
   */
  public MeMoXmlStreamLocation(MeMoXmlStreamLocation other) {
    this.elementStack = new ArrayList<>(other.elementStack);
  }

  /**
   * This method updates the internal stack and should be invoked when the cursor position has been moved
   * to a new MeMo class.
   *
   * @param xmlDepth the current depth in the xml document structure.
   * @param currentClass the current MeMo class
   */
  void updateStack(int xmlDepth, Class<? extends MeMoClass> currentClass) {
    if (xmlDepth < 1) {
      throw new IllegalArgumentException("depth must 1 or greater");
    }

    if (getDepth() >= xmlDepth) {
      updateStack(xmlDepth);
    }
    elementStack.add(currentClass);
  }

  /**
   * Returns the MeMo class at the current position.
   *
   * @return a MeMo class
   */
  public Class<? extends MeMoClass> getCurrentPosition() {
    if(elementStack.isEmpty()) {
      throw new IllegalStateException("The element stack is empty");
    }
    return elementStack.get(getDepth() - 1);
  }

  /**
   * Returns the parent MeMo class if available.
   *
   * @return the parent MeMo class
   */
  public Optional<Class<? extends MeMoClass>> getParentPosition() {
    if (getDepth() >= 2) {
      return Optional.of(elementStack.get(getDepth() - 2));
    } else {
      return Optional.empty();
    }
  }

  /**
   * Returns the current depth in the xml document structure at the current
   * position.
   *
   * @return a number representing the depth
   */
  public int getDepth() {
    return elementStack.size();
  }

  private void updateStack(int xmlDepth) {
    while (getDepth() >= xmlDepth) {
      elementStack.remove(getDepth() - 1);
    }
  }
}
