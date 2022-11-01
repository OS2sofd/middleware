package dk.digst.digital.post.memolib.writer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DefaultFileContentResolver implements FileContentResolver {

  @Override
  public InputStream resolve(String location) throws IOException {
    File file = new File(location);

    if (file.exists()) {
      return new FileInputStream(file);
    } else {
      return null;
    }
  }
}
