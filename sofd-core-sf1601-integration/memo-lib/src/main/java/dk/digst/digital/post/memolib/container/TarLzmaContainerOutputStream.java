package dk.digst.digital.post.memolib.container;

import dk.digst.digital.post.memolib.container.Constants.LzmaDictionarySize;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import lombok.NonNull;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.CompressorException;

/**
 * MeMoTarOutputStream is an implementation of {@link ContainerOutputStream}. It is a class which
 * can be used when the MeMo container is either a t file or a stream.
 */
class TarLzmaContainerOutputStream implements ContainerOutputStream {

  private final TarArchiveOutputStream tarOutputStream;

  private final ByteArrayOutputStream bufferOutputStream;

  private boolean openEntry = false;

  private @NonNull String entryKey;

  private TarEntryOutputStream entryOutputStream;

  private boolean closed = false;

  public TarLzmaContainerOutputStream(@NonNull OutputStream outputStream)
      throws IOException, CompressorException {
    this(outputStream, Constants.LZMA_DICTIONARY_DEFAULT_SIZE);
  }

  public TarLzmaContainerOutputStream(
      @NonNull OutputStream outputStream, LzmaDictionarySize lzmaDictionarySize)
      throws IOException, CompressorException {
    this.tarOutputStream =
        createArchiveOutputStream(
            new ConfigurableLzmaCompressorOutputStream(
                outputStream, lzmaDictionarySize.getLzmaOptions()));
    this.bufferOutputStream = new ByteArrayOutputStream(Constants.BUFFER_SIZE_IN_BYTES);
  }

  @Override
  public OutputStream writeNextEntry(@NonNull String entryKey) throws IOException {
    if (openEntry) {
      throw new IllegalStateException(
          "Unable to add new entry. Previous entry has not been closed");
    }
    if (closed) {
      throw new IOException("Stream has already been finished");
    }

    this.entryKey = entryKey;
    bufferOutputStream.reset();
    openEntry = true;
    entryOutputStream = new TarEntryOutputStream(bufferOutputStream);
    return entryOutputStream;
  }

  @Override
  public void closeEntry() throws IOException {
    if (openEntry) {
      TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(entryKey, false);
      byte[] bytes = bufferOutputStream.toByteArray();
      tarArchiveEntry.setSize(bytes.length);

      tarOutputStream.putArchiveEntry(tarArchiveEntry);
      tarOutputStream.write(bytes);
      tarOutputStream.closeArchiveEntry();
      entryOutputStream.close();
      entryOutputStream = null;
      openEntry = false;
    }
  }

  @Override
  public void close() throws IOException {
    tarOutputStream.close();
    openEntry = false;
    closed = true;
  }

  private TarArchiveOutputStream createArchiveOutputStream(OutputStream outputStream) {
    TarArchiveOutputStream archiveOutputStream =
        new TarArchiveOutputStream(
            outputStream, Constants.TAR_BLOCK_SIZE, Constants.TAR_ENCODING.name());
    archiveOutputStream.setAddPaxHeadersForNonAsciiNames(
        Constants.TAR_ADD_PAX_HEADERS_FOR_NON_ASCII_NAMES);
    archiveOutputStream.setLongFileMode(Constants.TAR_LONG_FILE_MODE);

    return archiveOutputStream;
  }
}
