package dk.digst.digital.post.memolib.container;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.compressors.CompressorException;

public class ArchiveEntryIteratorFactory {

  private AutoDetectArchiveInputStreamFactory autoDetectArchiveInputStreamFactory =
      new AutoDetectArchiveInputStreamFactory();

  public ArchiveEntryIterator newArchiveEntryIterator(InputStream inputStream)
      throws IOException, ArchiveException, CompressorException {
    return new ArchiveEntryIterator(
        autoDetectArchiveInputStreamFactory.newArchiveInputStream(inputStream));
  }
}
