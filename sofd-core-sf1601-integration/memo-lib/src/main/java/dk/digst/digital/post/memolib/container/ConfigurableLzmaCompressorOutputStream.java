package dk.digst.digital.post.memolib.container;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.LZMAOutputStream;

class ConfigurableLzmaCompressorOutputStream extends CompressorOutputStream {

  private final LZMAOutputStream out;

  /**
   * Creates a LZMA compressor.
   *
   * @param outputStream the stream to wrap
   * @throws IOException on error
   */
  public ConfigurableLzmaCompressorOutputStream(
      final OutputStream outputStream, LZMA2Options lzmaOptions) throws IOException {
    out = new LZMAOutputStream(outputStream, lzmaOptions, -1);
  }

  /** {@inheritDoc} */
  @Override
  public void write(final int b) throws IOException {
    out.write(b);
  }

  /** {@inheritDoc} */
  @Override
  public void write(final byte[] buf, final int off, final int len) throws IOException {
    out.write(buf, off, len);
  }

  /** Doesn't do anything as {@link LZMAOutputStream} doesn't support flushing. */
  @Override
  public void flush() throws IOException {}

  /**
   * Finishes compression without closing the underlying stream. No more data can be written to this
   * stream after finishing.
   *
   * @throws IOException on error
   */
  public void finish() throws IOException {
    out.finish();
  }

  /** {@inheritDoc} */
  @Override
  public void close() throws IOException {
    out.close();
  }
}
