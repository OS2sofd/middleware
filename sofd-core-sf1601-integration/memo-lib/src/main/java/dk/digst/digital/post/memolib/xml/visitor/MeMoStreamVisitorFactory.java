package dk.digst.digital.post.memolib.xml.visitor;

import dk.digst.digital.post.memolib.model.MeMoClass;
import dk.digst.digital.post.memolib.util.XmlStreamConsumer;
import dk.digst.digital.post.memolib.xml.parser.MeMoXmlStreamCursor;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * A factory to create a {@link MeMoStreamVisitor}. Two types of visitors can be created, one that
 * can consume the {@link MeMoXmlStreamCursor} by a {@link XmlStreamConsumer} and one that can parse
 * the MeMoClass and collect the object(s) internally.
 */
@UtilityClass
public class MeMoStreamVisitorFactory {

  /**
   * creates a {@link MeMoStreamVisitor}.
   *
   * @param clazz the MeMo class to visit
   * @param consumer the Consumer which should be invoked
   * @return a MeMoStreamVisitor
   */
  public <T extends MeMoClass> MeMoStreamVisitor createConsumer(
      @NonNull Class<T> clazz, @NonNull XmlStreamConsumer<MeMoXmlStreamCursor> consumer) {

    return new MeMoClassConsumer<>(clazz, null, consumer);
  }

  /**
   * creates a {@link MeMoStreamVisitor}.A parent class must be provided to further filter the MeMo
   * classes to consumed.
   *
   * @param clazz the MeMo class to visit
   * @param parentClazz the parent of the MeMo class to visit
   * @param consumer the Consumer which should be invoked
   * @return a MeMoStreamVisitor
   */
  public <T extends MeMoClass> MeMoStreamVisitor createConsumer(
      @NonNull Class<T> clazz,
      @NonNull Class<? extends MeMoClass> parentClazz,
      @NonNull XmlStreamConsumer<MeMoXmlStreamCursor> consumer) {

    return new MeMoClassConsumer<>(clazz, parentClazz, consumer);
  }

  /**
   * creates a {@link MeMoStreamProcessorVisitor} which can be used to parse and collect MeMo
   * classes from the xml stream.
   *
   * @param clazz the MeMo class to visit
   * @return a MeMoStreamVisitor
   */
  public <T extends MeMoClass> MeMoStreamProcessorVisitor<T> createProcessor(
      @NonNull Class<T> clazz) {

    return new MeMoClassProcessor<>(clazz, null);
  }

  /**
   * creates a {@link MeMoStreamProcessorVisitor} which can be used to parse and collect MeMo
   * classes from the xml stream. A parent class must be provided to further filter the MeMo classes
   * to be collected.
   *
   * @param clazz the MeMo class to visit
   * @param parentClazz the parent of the MeMo class to visit
   * @return
   */
  public <T extends MeMoClass> MeMoStreamProcessorVisitor<T> createProcessor(
      @NonNull Class<T> clazz, @NonNull Class<? extends MeMoClass> parentClazz) {

    return new MeMoClassProcessor<>(clazz, parentClazz);
  }
}
