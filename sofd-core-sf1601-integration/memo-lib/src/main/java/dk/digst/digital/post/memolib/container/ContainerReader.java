package dk.digst.digital.post.memolib.container;

import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;

/**
 * MeMoContainerReader can be used to read the entries of a MeMo container which holds one or more
 * MeMo message files can be extracted.
 */
public interface ContainerReader<T> extends Closeable {

  /**
   * @return an optional object. If there are no entries left in the file, an empty optional is
   *     returned.
   * @throws IOException if a problem occurs while reading the underlying stream
   */
  public Optional<T> readEntry() throws IOException;

  /**
   * Returns true if there is an entry at the current cursor position which can be read from the
   * container.
   *
   * @return true if there is an entry
   */
  public boolean hasEntry();
}
