package dk.digst.digital.post.memolib.container;

import dk.digst.digital.post.memolib.writer.MeMoStreamWriter;
import dk.digst.digital.post.memolib.writer.StreamWriter;
import java.io.Closeable;
import java.io.IOException;

public interface ContainerWriter<T> extends Closeable {

  /**
   * This method writes a single entry to a {@link TarLzmaContainerOutputStream}.
   *
   * @param name should be a unique name or identifier for this entry (within the context of the
   *     container)
   * @param entry the entry to be added as an entry
   * @return this
   * @throws IOException if a low-level I/O problem occurs
   */
  public ContainerWriter<T> writeEntry(String name, T entry)
      throws IOException;
  /**
   * This method adds a new entry to the container, but does not write the actual content of the
   * entry. Instead a {@link MeMoStreamWriter} is returned which must be used to write the actual
   * MeMo message. Before adding a new entry, it also makes sure that any entry being edited is
   * closed.
   *
   * @param name should be a unique name or identifier for this entry (within the context of the
   *     container)
   * @return MeMoStreamWriter which must be used to write the MeMo message entry
   * @throws IOException if a low-level I/O problem occurs
   */
  public StreamWriter<T> getWriterForNextEntry(String name) throws IOException;
  
}
