package dk.digst.digital.post.memolib.container;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.compressors.CompressorException;

public interface ContainerReaderFactory<T> {

  ContainerReader<T> newContainerReader(InputStream inputStream)
      throws IOException, ArchiveException, CompressorException;
}
