package dk.digst.digital.post.memolib.container;

import dk.digst.digital.post.memolib.container.LzmaUtils.LzmaStatistics;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

class AutoDetectArchiveInputStreamFactory {

  public ArchiveInputStream newArchiveInputStream(InputStream inputStream)
      throws IOException, CompressorException {
    InputStream bufferedInputStream = createBufferedInputStream(inputStream);

    InputStream compressorInputStream =
        createBufferedInputStream(newCompressorInputStream(bufferedInputStream));

    return createArchiveInpuStream(compressorInputStream);
  }

  private InputStream newCompressorInputStream(InputStream inputStream)
      throws CompressorException, IOException {
    String compressFormat = detectCompressFormat(inputStream);

    return createLzmaCompressorStreamFactory(inputStream)
        .createCompressorInputStream(compressFormat, inputStream);
  }

  private CompressorStreamFactory createLzmaCompressorStreamFactory(InputStream inputStream)
      throws IOException {
    LzmaStatistics statistics = LzmaUtils.getStatistics(inputStream);

    int dictSize = statistics.getDictSize();

    if (dictSize > Constants.LZMA_DICTIONARY_MAX_SIZE.getSizeInBytes()) {
      throw new IllegalStateException(
          "Dictionary size is too large - unsupported! (dictionary size: "
              + dictSize / 1024
              + " kB)");
    }

    return new CompressorStreamFactory(false, LzmaUtils.getMaxMemoryInKb(dictSize));
  }

  private ArchiveInputStream createArchiveInpuStream(InputStream inputStream) {
    String archiveFormat = null;

    try {
      archiveFormat = ArchiveStreamFactory.detect(inputStream);
    } catch (ArchiveException e) {
      throw new IllegalStateException("Unable to detect archive format (archive must not be empty)!", e);
    }

    if (!ArchiveStreamFactory.TAR.equals(archiveFormat)) {
      throw new IllegalStateException(
          String.format("Illegal archive format: %s, expection TAR", archiveFormat));
    }

    return new TarArchiveInputStream(
        inputStream,
        Constants.TAR_BLOCK_SIZE,
        Constants.TAR_RECORD_SIZE,
        Constants.TAR_ENCODING.name(),
        Constants.TAR_LENIENT);
  }

  private static String detectCompressFormat(InputStream inputStream) {
    String compressFormat;

    try {
      compressFormat = CompressorStreamFactory.detect(inputStream);
    } catch (CompressorException e) {
      throw new IllegalStateException("Unable to detect compression format", e);
    }

    if (!CompressorStreamFactory.LZMA.equals(compressFormat)) {
      throw new IllegalStateException(
          String.format("Illegal compression format: %s, expecting LZMA", compressFormat));
    }

    return compressFormat;
  }

  private InputStream createBufferedInputStream(InputStream inputStream) {
    return new BufferedInputStream(inputStream, Constants.BUFFER_SIZE_IN_BYTES);
  }
}
