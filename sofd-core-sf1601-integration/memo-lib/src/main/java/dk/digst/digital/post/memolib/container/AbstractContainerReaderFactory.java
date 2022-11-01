package dk.digst.digital.post.memolib.container;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.compressors.CompressorException;

public class AbstractContainerReaderFactory<T> implements ContainerReaderFactory<T> {

  private final ArchiveEntryIteratorFactory archiveEntryIteratorFactory =
      new ArchiveEntryIteratorFactory();

  private final Function<ArchiveEntryIterator, ContainerReader<T>> containerReaderProducer;

  protected AbstractContainerReaderFactory(
      Function<ArchiveEntryIterator, ContainerReader<T>> containerReaderProducer) {
    this.containerReaderProducer = containerReaderProducer;
  }

  public ContainerReader<T> newContainerReader(File file)
      throws IOException, ArchiveException, CompressorException {
    return newContainerReader(new FileInputStream(file));
  }

  @Override
  public ContainerReader<T> newContainerReader(InputStream inputStream)
      throws IOException, ArchiveException, CompressorException {

    return containerReaderProducer.apply(
        archiveEntryIteratorFactory.newArchiveEntryIterator(inputStream));
  }
}
