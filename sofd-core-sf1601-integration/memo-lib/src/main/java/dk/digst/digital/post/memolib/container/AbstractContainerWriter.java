package dk.digst.digital.post.memolib.container;

import dk.digst.digital.post.memolib.writer.MeMoStreamWriter;
import dk.digst.digital.post.memolib.writer.MeMoWriteException;
import dk.digst.digital.post.memolib.writer.StreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Function;
import lombok.NonNull;

public abstract class AbstractContainerWriter<T> implements ContainerWriter<T> {

  private final ContainerOutputStream meMoContainer;

  private final Function<OutputStream, StreamWriter<T>> writerProducer;

  private StreamWriter<T> activeWriter;

  AbstractContainerWriter(
      @NonNull ContainerOutputStream meMoContainer,
      Function<OutputStream, StreamWriter<T>> writerProducer) {
    this.writerProducer = writerProducer;
    this.meMoContainer = meMoContainer;
  }

  /**
   * This method writes a single entry to a {@link ContainerOutputStream}.
   *
   * @param name should be a unique name or identifier for this entry (within the context of the
   *     container)
   * @param entry the message to be added as an entry
   * @return this
   * @throws IOException if a low-level I/O problem occurs
   * @throws MeMoWriteException if an underlying problem related to the xml stream occurs
   */
  @Override
  public AbstractContainerWriter<T> writeEntry(String name, T entry)
      throws IOException, MeMoWriteException {
    StreamWriter<T> streamWriter = getWriterForNextEntry(name);

    streamWriter.write(entry);

    closeCurrentlyOpenEntry();

    return this;
  }

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
  @Override
  public StreamWriter<T> getWriterForNextEntry(String name) throws IOException {
    if (isWritingMeMoMessage()) {
      closeCurrentlyOpenEntry();
    }

    OutputStream outputStream = meMoContainer.writeNextEntry(name);
    activeWriter = writerProducer.apply(outputStream);

    return activeWriter;
  }

  @Override
  public void close() throws IOException {
    if (isWritingMeMoMessage()) {
      closeCurrentlyOpenEntry();
    }

    meMoContainer.close();
  }

  private boolean isWritingMeMoMessage() {
    return activeWriter != null;
  }

  private void closeCurrentlyOpenEntry() throws IOException {
    /* close the writer to close and flush the xml stream */
    activeWriter.close();

    /* close the entry in the memo container */
    meMoContainer.closeEntry();

    this.activeWriter = null;
  }
}
