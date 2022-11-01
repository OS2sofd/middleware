package dk.digst.digital.post.memolib.container;

import java.io.Closeable;
import java.io.IOException;

/** An iterator of {@link ContainerEntry} object instances. */
interface IterableContainer extends Closeable {

  boolean hasEntry();

  ContainerEntry getEntry();

  void nextEntry() throws IOException;
}
