package dk.digst.digital.post.memolib.container;

import java.io.IOException;
import lombok.Getter;
import lombok.NonNull;

public abstract class AbstractContainerReader<T> implements ContainerReader<T> {

  @Getter
  private final IterableContainer iterableContainer;

  protected AbstractContainerReader(@NonNull IterableContainer iterableContainer) {
    this.iterableContainer = iterableContainer;
  }

  @Override
  public boolean hasEntry() {
    return iterableContainer.hasEntry();
  }

  @Override
  public void close() throws IOException {
    iterableContainer.close();
  }
}
