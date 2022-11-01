package dk.digst.digital.post.memolib.writer;

import java.io.IOException;
import java.io.InputStream;

public interface FileContentResolver {

  InputStream resolve(String location) throws IOException;
}
