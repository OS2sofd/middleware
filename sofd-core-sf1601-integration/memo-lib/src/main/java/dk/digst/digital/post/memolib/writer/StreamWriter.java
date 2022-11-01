package dk.digst.digital.post.memolib.writer;

import java.io.Closeable;
import java.io.IOException;

/** MeMoStreamWriter supports writing a MeMo message as a stream. */
public interface StreamWriter<T> extends Closeable {

  /**
   * This method writes the element to the
   *
   * @param entry the entry to be written.
   * @throws IOException if a low-level I/O problem occurs
   */
  void write(T entry) throws IOException;

  /**
   * This method can be used to determine if the StreamWriter is closed.
   *
   * @return a boolean indicating if the MeMoStreamWriter is closed.
   */
  boolean isClosed();

  /**
   * This method closes both the StreamWriter and the underlying outputStream.
   *
   * @throws IOException if a low-level I/O problem occurs
   */
  void closeStream() throws IOException;

  FileContentLoader getFileContentLoader();
}
