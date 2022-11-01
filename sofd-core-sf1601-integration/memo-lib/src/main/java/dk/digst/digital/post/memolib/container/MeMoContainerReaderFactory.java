package dk.digst.digital.post.memolib.container;

import dk.digst.digital.post.memolib.model.Message;

public class MeMoContainerReaderFactory extends AbstractContainerReaderFactory<Message> {

  public MeMoContainerReaderFactory() {
    super(MeMoContainerReader::new);
  }
}
