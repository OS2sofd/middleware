package dk.digst.digital.post.memolib.container;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

/** A MeMoContainerOutputStream allows entries to be written to a MeMo container. */
public interface ContainerOutputStream extends Closeable {

  /**
   * This method must be used when adding an entry to the container.
   *
   * @param key the identifier of the entry. It could be a file name or another unique identifier
   * @return an OutputStream which must be used to write the content of the entry
   * @throws IOException if a low-level I/O problem occurs
   */
  OutputStream writeNextEntry(String key) throws IOException;

  /**
   * This method must be used when the processing of the entry is complete and the content has been
   * written. The OutputStream must not be closed directly.
   *
   * @throws IOException if a low-level I/O problem occurs
   */
  void closeEntry() throws IOException;
}
