package dk.digst.digital.post.memolib.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Base64;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Base64StreamEncoder {

  private final int ENCODING_BUFFER_SIZE = 3 * 1024;

  public void encodeAsBase64(InputStream inputStream, OutputStream outputStream)
      throws IOException {

    BufferedInputStream bis = new BufferedInputStream(inputStream, ENCODING_BUFFER_SIZE);
    Base64.Encoder base64Encoder = Base64.getEncoder();

    byte[] buffer = new byte[ENCODING_BUFFER_SIZE];
    int bytesRead;
    while ((bytesRead = bis.read(buffer)) == ENCODING_BUFFER_SIZE) {
      outputStream.write(base64Encoder.encode(buffer));
    }
    if (bytesRead > 0) {
      buffer = Arrays.copyOf(buffer, bytesRead);
      outputStream.write(base64Encoder.encode(buffer));
    }

    outputStream.flush();
  }
}
