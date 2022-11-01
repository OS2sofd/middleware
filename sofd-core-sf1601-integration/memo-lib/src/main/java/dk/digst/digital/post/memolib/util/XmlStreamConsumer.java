package dk.digst.digital.post.memolib.util;

import java.io.IOException;
import javax.xml.stream.XMLStreamException;

@FunctionalInterface
public interface XmlStreamConsumer<T> {
  void accept(T t) throws XMLStreamException, IOException;
}
