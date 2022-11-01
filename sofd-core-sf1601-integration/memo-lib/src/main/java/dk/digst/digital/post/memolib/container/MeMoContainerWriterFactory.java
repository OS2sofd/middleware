package dk.digst.digital.post.memolib.container;

import dk.digst.digital.post.memolib.container.Constants.LzmaDictionarySize;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.compress.compressors.CompressorException;

public class MeMoContainerWriterFactory {

  public MeMoContainerWriter newMeMoContainerWriter(OutputStream outputStream)
      throws IOException, CompressorException {

    return new MeMoContainerWriter(new TarLzmaContainerOutputStream(outputStream));
  }

  public MeMoContainerWriter newMeMoContainerWriter(
      OutputStream outputStream, LzmaDictionarySize lzmaDictionarySize)
      throws IOException, CompressorException {

    return new MeMoContainerWriter(new TarLzmaContainerOutputStream(outputStream, lzmaDictionarySize));
  }
}
