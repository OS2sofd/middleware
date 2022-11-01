package dk.digst.digital.post.memolib.container;

import java.io.InputStream;

public interface ContainerEntry {

  /**
   * @return the key of the entry
   */
  String getKey();

  /**
   * Returns the underlying input stream
   *
   * @return an input stream
   */
  InputStream stream();

  /**
   * @return the size of the entry
   */
  long getSize();
}
