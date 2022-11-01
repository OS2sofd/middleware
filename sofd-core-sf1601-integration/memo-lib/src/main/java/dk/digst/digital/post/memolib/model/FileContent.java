package dk.digst.digital.post.memolib.model;

import java.io.IOException;
import java.io.InputStream;

public interface FileContent {

  String location();

  boolean canBeStreamed();

  InputStream streamContent() throws IOException;

  boolean isBase64encoded();
}
