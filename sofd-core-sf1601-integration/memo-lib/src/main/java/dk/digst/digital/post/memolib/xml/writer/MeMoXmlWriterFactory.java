package dk.digst.digital.post.memolib.xml.writer;

import dk.digst.digital.post.memolib.writer.DefaultFileContentResolver;
import dk.digst.digital.post.memolib.writer.FileContentLoader;
import dk.digst.digital.post.memolib.writer.MeMoStreamWriter;
import dk.digst.digital.post.memolib.xml.mapper.ObjectMapperProvider;
import dk.digst.digital.post.memolib.xml.schema.MeMoValidationSchemaProvider;
import dk.digst.digital.post.memolib.xml.stax.Stax2FactoryProvider;
import java.io.IOException;
import java.io.OutputStream;
import javax.xml.stream.XMLStreamException;
import org.codehaus.stax2.validation.XMLValidationSchema;

/** MeMoWriterFactory can be used to create an instance of a {@link MeMoStreamWriter}. */
public class MeMoXmlWriterFactory {

  /* private constructor to prevent instantiation of the factory */
  private MeMoXmlWriterFactory() {}

  /**
   * This method creates a {@link MeMoStreamWriter} which will use the provided {@link OutputStream}
   * to write the MeMo message without performing validation. The writer will use a basic {@link
   * FileContentLoader} which uses {@link DefaultFileContentResolver} to resolve the file content.
   *
   * @param outputStream the stream to be written to
   * @return a MeMoStreamXmlWriter
   * @throws IOException if a low-level I/O problem occurs
   */
  public static MeMoStreamWriter createWriter(OutputStream outputStream) throws IOException {
    return createWriter(outputStream, false);
  }

  /**
   * This method creates a {@link MeMoStreamWriter} which will use the provided {@link OutputStream}
   * to write the MeMo message. The writer will use a basic {@link FileContentLoader} which uses
   * {@link DefaultFileContentResolver} to resolve the file content. In addition the method
   * signature accepts boolean flag to indicate whether validation should be performed
   *
   * @param outputStream the stream to be written to
   * @param enableValidation if true MeMo will be validated upon writing
   * @return a MeMoStreamXmlWriter
   * @throws IOException if a low-level I/O problem occurs
   */
  public static MeMoStreamWriter createWriter(OutputStream outputStream, boolean enableValidation)
      throws IOException {
    return createWriter(outputStream, new FileContentLoader(), enableValidation);
  }

  /**
   * This method creates a {@link MeMoStreamWriter} which will use the provided {@link OutputStream}
   * to write the MeMo message without performing validation. The method also expects a {@link
   * FileContentLoader} which can be used to load external file content resources.
   *
   * @param outputStream the stream to be written to
   * @param fileContentLoader a loader which can be used to load external file content resources
   * @return a MeMoStreamXmlWriter
   * @throws IOException if a low-level I/O problem occurs
   */
  public static MeMoStreamWriter createWriter(
      OutputStream outputStream, FileContentLoader fileContentLoader) throws IOException {
    return createWriter(outputStream, fileContentLoader, false);
  }

  /**
   * This method creates a {@link MeMoStreamWriter} which will use the provided {@link OutputStream}
   * to write the MeMo message. The method also expects a {@link FileContentLoader} which can be
   * used to load external file content resources. In addition the method signature accepts boolean
   * flag to indicate whether validation should be performed
   *
   * @param outputStream the stream to be written to
   * @param fileContentLoader a loader which can be used to load external file content resources
   * @param enableValidation if true MeMo will be validated upon writing
   * @return a MeMoStreamXmlWriter
   * @throws IOException if a low-level I/O problem occurs
   */
  public static MeMoStreamWriter createWriter(
      OutputStream outputStream, FileContentLoader fileContentLoader, boolean enableValidation)
      throws IOException {
    try {
      return new MeMoStreamWriterImpl(
          outputStream,
          Stax2FactoryProvider.getXmlOutputFactory(),
          ObjectMapperProvider.getXmlMapper(),
          getValidationSchema(enableValidation),
          fileContentLoader);
    } catch (XMLStreamException e) {
      throw new IOException(e);
    }
  }

  private static XMLValidationSchema getValidationSchema(boolean enableValidation)
      throws XMLStreamException {
    return enableValidation ? MeMoValidationSchemaProvider.getMeMoValidationSchema() : null;
  }
}
