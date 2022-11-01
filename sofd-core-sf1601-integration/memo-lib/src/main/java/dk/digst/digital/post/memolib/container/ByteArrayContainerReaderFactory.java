package dk.digst.digital.post.memolib.container;

public class ByteArrayContainerReaderFactory extends AbstractContainerReaderFactory<byte[]> {

  public ByteArrayContainerReaderFactory() {
    super(ByteArrayContainerReader::new);
  }
}
