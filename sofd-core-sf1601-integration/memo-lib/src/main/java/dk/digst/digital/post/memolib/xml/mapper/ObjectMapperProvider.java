package dk.digst.digital.post.memolib.xml.mapper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import dk.digst.digital.post.memolib.model.FileContent;
import dk.digst.digital.post.memolib.model.IncludedFileContent;
import dk.digst.digital.post.memolib.xml.writer.CustomNamespaceAnnotationIntrospector;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The ObjectMapperProvider provides properly configured Object- and XmlMapper objects used by the
 * XML package.
 */
public class ObjectMapperProvider {

  public static final String UTC_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
  private static XmlMapper xmlMapper;

  private ObjectMapperProvider() {}

  /**
   * This method returns a properly configured XmlMapper.
   *
   * @return a XmlMapper
   */
  public static XmlMapper getXmlMapper() {
    if (xmlMapper == null) {
      JacksonXmlModule xmlModule = new JacksonXmlModule();

      /*  Do not use a wrapper element for collections */
      xmlModule.setDefaultUseWrapper(false);

      XmlMapper xmlMapperTmp = new XmlMapper(xmlModule);
      JavaTimeModule module = new JavaTimeModule();

      /* We must override the default serializer for LocalDateTime since we want the time to
       * be represented with the UTC offset in the serialized XML message */
      LocalDateTimeSerializer serializer =
          new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(UTC_DATE_TIME_FORMAT));
      module.addSerializer(LocalDateTime.class, serializer);
      xmlMapperTmp.registerModule(module);

      /* Deserialization specific settings - resolve File content */
      SimpleModule fileContentModule = configureFileContentModule();
      xmlMapperTmp.registerModule(fileContentModule);

      /* Serialization specific settings */

      /* add custom annotation introspector which changes the way namespaces are resolved */
      xmlMapperTmp.setSerializationInclusion(JsonInclude.Include.NON_NULL);
      xmlMapperTmp.setAnnotationIntrospector(
          new CustomNamespaceAnnotationIntrospector(xmlMapperTmp, false));
      
      xmlMapper = xmlMapperTmp;
    }

    return xmlMapper;
  }

  private static SimpleModule configureFileContentModule() {
    SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();

    /* XML model mappings */
    /* This mapping is used when deserializing an XML message */
    resolver.addMapping(FileContent.class, IncludedFileContent.class);

    SimpleModule fileContentModule = new SimpleModule();
    fileContentModule.setAbstractTypes(resolver);
    return fileContentModule;
  }
}
