package dk.digst.digital.post.memolib.writer;

import dk.digst.digital.post.memolib.model.FileContent;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;

public class FileContentLoader {

  private final List<FileContentResolver> resolvers;

  public FileContentLoader() {
    resolvers = new ArrayList<>();
    resolvers.add(new DefaultFileContentResolver());
  }

  public FileContentLoader(@NonNull List<FileContentResolver> resolvers) {
    this.resolvers = resolvers;
  }

  public InputStream resolveContent(FileContent readableFileContent) throws IOException {
    if (readableFileContent.canBeStreamed()) {
      return readableFileContent.streamContent();
    } else {
      for (FileContentResolver resolver : resolvers) {
        InputStream inputStream = resolver.resolve(readableFileContent.location());
        if (inputStream != null) {
          return inputStream;
        }
      }
      throw new IOException("Content could not be resolved");
    }
  }

  public List<FileContentResolver> getResolvers() {
    return resolvers;
  }
}
