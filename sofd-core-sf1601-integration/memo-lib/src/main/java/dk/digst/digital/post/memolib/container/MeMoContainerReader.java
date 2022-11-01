package dk.digst.digital.post.memolib.container;

import dk.digst.digital.post.memolib.model.Message;
import dk.digst.digital.post.memolib.parser.MeMoParseException;
import dk.digst.digital.post.memolib.parser.MeMoParser;
import dk.digst.digital.post.memolib.parser.MeMoParserFactory;
import java.io.IOException;
import java.util.Optional;
import lombok.NonNull;

/**
 * MeMoContainerReader can be used to read the entries of a MeMo container which holds one or more
 * MeMo message files can be extracted.
 */
public class MeMoContainerReader extends AbstractContainerReader<Message> {

  MeMoContainerReader(@NonNull IterableContainer meMoContainerEntryIterator) {
    super(meMoContainerEntryIterator);
  }

  /**
   * A method which parses the current entry and returns a {@link Message}. It moves the
   * MeMoContainerEntryCursor to the next entry when the current entry has been unmarshalled.
   *
   * @return an optional Message object. If there are no entries left in the file, an empty optional
   *     is returned.
   * @throws IOException if a problem occurs while reading the underlying stream
   * @throws MeMoParseException if a problem occurs while parsing the current entry in the container
   */
  @Override
  public Optional<Message> readEntry() throws IOException, MeMoParseException {
    if (!getIterableContainer().hasEntry()) {
      return Optional.empty();
    }

    ContainerEntry currentEntry = getIterableContainer().getEntry();

    MeMoParser parser = null;

    try {
      parser = MeMoParserFactory.createParser(currentEntry.stream());
      Optional<Message> message = Optional.of(parser.parse());
      getIterableContainer().nextEntry();

      return message;

    } finally {
      if (parser != null) {
        parser.close();
      }
    }
  }
}
