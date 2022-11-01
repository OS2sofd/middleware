package dk.digst.digital.post.memolib.container;

import java.io.IOException;
import java.io.OutputStream;

class TarEntryOutputStream extends OutputStream {

  private OutputStream outputStream;

  private boolean closed = false;

  public TarEntryOutputStream(OutputStream outputStream) {
    this.outputStream = outputStream;
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    checkNotClosed();
    outputStream.write(b, off, len);
  }

  @Override
  public void write(int b) throws IOException {
    checkNotClosed();
    outputStream.write(b);
  }

  @Override
  public void close() throws IOException {
    closed = true;
    super.close();
  }

  private void checkNotClosed() {
    if (closed) {
      throw new IllegalStateException("Entry stream has been closed");
    }
  }
}
