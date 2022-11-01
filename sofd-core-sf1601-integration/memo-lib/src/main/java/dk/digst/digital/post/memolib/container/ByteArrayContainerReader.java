package dk.digst.digital.post.memolib.container;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import lombok.NonNull;
import org.apache.commons.compress.utils.IOUtils;

/**
 * MeMoContainerReader can be used to read the entries of a MeMo container which holds one or more
 * MeMo message files can be extracted.
 */
public class ByteArrayContainerReader extends AbstractContainerReader<byte[]> {

  ByteArrayContainerReader(@NonNull IterableContainer iterableContainer) {
    super(iterableContainer);
  }

  @Override
  public Optional<byte[]> readEntry() throws IOException {
    Optional<byte[]> optional = Optional.empty();

    if (getIterableContainer().hasEntry()) {
      ContainerEntry currentEntry = getIterableContainer().getEntry();

      try (InputStream inputStream = currentEntry.stream()) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, outputStream);
        optional = Optional.ofNullable(outputStream.toByteArray());
      }
    }

    return optional;
  }
}
