package dk.digst.digital.post.memolib.container;

import dk.digst.digital.post.memolib.model.Message;
import dk.digst.digital.post.memolib.writer.MeMoWriterFactory;
import lombok.NonNull;

/** MeMoContainerWriter writes entries to a {@link ContainerOutputStream} */
public class MeMoContainerWriter extends AbstractContainerWriter<Message> {

  MeMoContainerWriter(@NonNull ContainerOutputStream meMoContainer) {
    super(meMoContainer, MeMoWriterFactory::createWriter);
  }
}
