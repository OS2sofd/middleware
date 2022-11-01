package dk.digst.digital.post.memolib.parser;

import javax.xml.stream.XMLStreamException;

@SuppressWarnings("serial")
public class MeMoParseException extends RuntimeException {
  
  public MeMoParseException(String message) {
    super(message);
  }

  public MeMoParseException(XMLStreamException e) {
    super(e);
  }
}
