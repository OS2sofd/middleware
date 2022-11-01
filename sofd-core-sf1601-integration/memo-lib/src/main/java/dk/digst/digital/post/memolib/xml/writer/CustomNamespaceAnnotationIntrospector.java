package dk.digst.digital.post.memolib.xml.writer;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.commons.lang3.StringUtils;

import static com.fasterxml.jackson.databind.introspect.AnnotatedClassResolver.resolve;
import static dk.digst.digital.post.memolib.model.Namespace.MEMO;

/**
 * The class extends the standard {@link JacksonXmlAnnotationIntrospector} and is tailored to handle
 * the MeMo message format and more specifically how namespace are used. The intent is to limit the
 * amount of annotations needed on the xml model classes.
 *
 * <p>The behaviour of the default Jackson annotations is changed to the following:
 *
 * <p>If a field is not annotated, it will resolve the namespace by inspecting the annotations on
 * class-level. If no class-level annotation exist, it will default to the MeMo namespace.
 */
@SuppressWarnings("serial")
public class CustomNamespaceAnnotationIntrospector extends JacksonXmlAnnotationIntrospector {
  private final XmlMapper mapper;

  public CustomNamespaceAnnotationIntrospector(XmlMapper mapper, boolean defaultUseWrapper) {
    /* This is required to configure what JacksonXmlAnnotationIntrospector returns as default
     * when deciding to wrap collections during serialization. */
    super(defaultUseWrapper);
    this.mapper = mapper;
  }

  /**
   * This methods tries to resolve the local name and namespace to be used for the specified class
   * if it is configured. If not, it will default to the MeMo namespace.
   *
   * @param ac the annotated class
   * @return the PropertyName containing which local name and namespace to use for this class
   */
  @Override
  public PropertyName findRootName(AnnotatedClass ac) {
    PropertyName pn = super.findRootName(ac);
    if (pn == null) {
      /* default to memo namespace */
      return new PropertyName(ac.getRawType().getSimpleName(), MEMO);
    }
    return pn;
  }

  /**
   * The method resolves the namespace of the annotated element by using the default method of the
   * {@link JacksonXmlAnnotationIntrospector}. If the namespace can't be resolved, it will try to
   * resolve it on class-level if these preconditions are met:
   *
   * <p>1. The annotated element is a method.
   *
   * <p>2. The annotated element should not be handled as an attribute when serializing to xml.
   *
   * @param ann the annotated class member
   * @return The resolved namespace of the annotated class member
   */
  @Override
  public String findNamespace(MapperConfig<?> config, Annotated ann) {
    String namespace = super.findNamespace(config, ann);

    /* if the namespace has not been declared try and resolve the name space on class level instead
     * Unless the field should be written as an attribute. In that case, no namespace prefix should be
     * returned.
     */
    boolean shouldWriteAsElement = !Boolean.TRUE.equals(super.isOutputAsAttribute(config, ann));

      if (StringUtils.isEmpty(namespace) && shouldWriteAsElement && ann instanceof AnnotatedMethod) {
      /* if the namespace has not been declared on the field, look for a declaration on the
       * class level instead */
      Class<?> declaringClass = ((AnnotatedMethod) ann).getDeclaringClass();
      JavaType javaType = mapper.getTypeFactory().constructSimpleType(declaringClass, null);
      AnnotatedClass resolvedClass = resolve(mapper.getSerializationConfig(), javaType, null);
      return findRootName(resolvedClass).getNamespace();
    } else {
      return namespace;
    }
  }
}
