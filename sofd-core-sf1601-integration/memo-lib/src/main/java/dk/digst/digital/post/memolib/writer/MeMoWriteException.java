package dk.digst.digital.post.memolib.writer;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import javax.xml.stream.XMLStreamException;

@SuppressWarnings("serial")
public class MeMoWriteException extends RuntimeException {

  public MeMoWriteException(String message) {
    super(message);
  }

  public MeMoWriteException(XMLStreamException e) {
    super(e);
  }

  public MeMoWriteException(JsonGenerationException e) {
    super(e.getMessage(), e);
  }

  public MeMoWriteException(JsonMappingException e) {
    super(e.getMessage(), e);
  }
}
