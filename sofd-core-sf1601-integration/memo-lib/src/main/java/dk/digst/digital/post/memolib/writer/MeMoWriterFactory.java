package dk.digst.digital.post.memolib.writer;

import dk.digst.digital.post.memolib.xml.writer.MeMoXmlWriterFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;

/** MeMoWriterFactory can be used to create an instance of a {@link MeMoStreamWriter}. */
public class MeMoWriterFactory {

  private static final List<FileContentResolver> FILE_CONTENT_RESOLVERS = new ArrayList<>();

  /* private constructor to prevent instantiation of the factory */
  private MeMoWriterFactory() {}

  public static void clearResourceResolvers() {
    FILE_CONTENT_RESOLVERS.clear();
  }

  public static void registerFileContentResourceResolver(FileContentResolver resolver) {
    if (FILE_CONTENT_RESOLVERS
        .stream()
        .noneMatch(
            registeredResolver -> registeredResolver.getClass().equals(resolver.getClass()))) {
      FILE_CONTENT_RESOLVERS.add(resolver);
    }
  }

  /**
   * This method creates a {@link MeMoStreamWriter} which will use the provided {@link OutputStream}
   * to write the MeMo message.
   *
   * @param outputStream the stream to be written to
   * @return MeMoStreamWriter
   * @throws UncheckedIOException if a low-level I/O problem occurs
   */
  public static MeMoStreamWriter createWriter(@NonNull OutputStream outputStream) {
    return createWriter(outputStream, false);
  }

  /**
   * This method creates a {@link MeMoStreamWriter} which will use the provided {@link OutputStream}
   * to write the MeMo message. In addition the method signature accepts boolean flag to indicate
   * whether validation should be performed
   *
   * @param outputStream the stream to be written to
   * @param enableValidation if true MeMo will be validated upon writing
   * @return MeMoStreamWriter
   * @throws UncheckedIOException if a low-level I/O problem occurs
   */
  public static MeMoStreamWriter createWriter(
      @NonNull OutputStream outputStream, boolean enableValidation) {

    List<FileContentResolver> resolvers = new ArrayList<>(FILE_CONTENT_RESOLVERS);
    resolvers.add(new DefaultFileContentResolver());
    FileContentLoader fileContentLoader = new FileContentLoader(resolvers);

    try {
      return MeMoXmlWriterFactory.createWriter(outputStream, fileContentLoader, enableValidation);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
