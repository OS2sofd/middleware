package dk.digst.digital.post.memolib.writer;

import dk.digst.digital.post.memolib.model.Message;
import java.io.IOException;

/** MeMoStreamWriter supports writing a MeMo message as a stream. */
public interface MeMoStreamWriter extends StreamWriter<Message> {

  /**
   * This method writes the {@link Message} element to the
   *
   * @param message the message to be written.
   * @throws MeMoWriteException if an error occurs while writing the message
   * @throws IOException if a low-level I/O problem occurs
   */
  @Override
  void write(Message message) throws MeMoWriteException, IOException;

  /**
   * This method can be used to determine if the MeMoStreamWriter is closed.
   *
   * @return a boolean indicating if the MeMoStreamWriter is closed.
   */
  @Override
  boolean isClosed();

  /**
   * This method closes both the MeMoStreamWriter and the underlying outputStream.
   *
   * @throws IOException if a low-level I/O problem occurs
   */
  @Override
  void closeStream() throws IOException;

  @Override
  FileContentLoader getFileContentLoader();
}
