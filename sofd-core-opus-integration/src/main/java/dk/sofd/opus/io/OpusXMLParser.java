package dk.sofd.opus.io;

import java.io.Reader;

import javax.xml.bind.JAXBContext;

import org.springframework.stereotype.Component;

import dk.kmd.opus.Kmd;

@Component
public class OpusXMLParser {

    public Kmd parseXML(Reader reader) throws Exception {
        JAXBContext context = JAXBContext.newInstance(Kmd.class);

        return (Kmd) context.createUnmarshaller().unmarshal(reader);
    }
}
