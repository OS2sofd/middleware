package dk.digst.digital.post.memolib.container;

import dk.digst.digital.post.memolib.parser.MeMoParser;
import java.io.IOException;
import lombok.NonNull;
import org.apache.commons.compress.archivers.ArchiveInputStream;

/**
 * The ArchiveEntryIterator implements {@link IterableContainer} and can be used to process the
 * entries of an {@link ArchiveInputStream}. For now it can be used to iterate through the entries
 * and get a reference to the input stream, which can be used by a {@link MeMoParser} to parse the
 * individual MeMo message.
 */
public class ArchiveEntryIterator implements IterableContainer {

  private final ArchiveInputStream archiveInputStream;

  private org.apache.commons.compress.archivers.ArchiveEntry entry;

  public ArchiveEntryIterator(@NonNull ArchiveInputStream archiveInputStream) throws IOException {
    this.archiveInputStream = archiveInputStream;
    this.entry = archiveInputStream.getNextEntry();
  }

  @Override
  public boolean hasEntry() {
    return entry != null;
  }

  @Override
  public void close() throws IOException {
    archiveInputStream.close();
  }

  @Override
  public void nextEntry() throws IOException {
    this.entry = archiveInputStream.getNextEntry();
  }

  @Override
  public ContainerEntry getEntry() {
    return new ArchiveEntry(entry, archiveInputStream);
  }
}
