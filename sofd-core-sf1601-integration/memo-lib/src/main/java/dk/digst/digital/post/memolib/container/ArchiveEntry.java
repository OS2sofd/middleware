package dk.digst.digital.post.memolib.container;

import java.io.InputStream;
import lombok.NonNull;
import org.apache.commons.compress.archivers.ArchiveInputStream;

class ArchiveEntry implements ContainerEntry {

  private final org.apache.commons.compress.archivers.ArchiveEntry entry;

  private final ArchiveInputStream archiveInputStream;

  ArchiveEntry(
      org.apache.commons.compress.archivers.ArchiveEntry entry,
      @NonNull ArchiveInputStream archiveInputStream) {
    this.entry = entry;
    this.archiveInputStream = archiveInputStream;
  }

  /**
   * Returns the filename of the entry in the archive file.
   *
   * @return the filename of the archive entry
   */
  @Override
  public String getKey() {
    return this.entry != null ? this.entry.getName() : null;
  }

  @Override
  public InputStream stream() {
    return archiveInputStream;
  }

  @Override
  public long getSize() {
    return this.entry != null ? this.entry.getSize() : 0;
  }
}
