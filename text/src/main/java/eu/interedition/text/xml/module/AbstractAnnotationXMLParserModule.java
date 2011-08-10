package eu.interedition.text.xml.module;

import com.google.common.collect.Maps;
import eu.interedition.text.Annotation;
import eu.interedition.text.AnnotationRepository;
import eu.interedition.text.QName;
import eu.interedition.text.xml.XMLParserState;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public abstract class AbstractAnnotationXMLParserModule extends XMLParserModuleAdapter {
  protected final AnnotationRepository annotationRepository;
  protected final int batchSize;

  private final ThreadLocal<Map<Annotation, Map<QName, String>>> annotationBatch = new ThreadLocal<Map<Annotation, Map<QName, String>>>();

  protected AbstractAnnotationXMLParserModule(AnnotationRepository annotationRepository, int batchSize) {
    this.annotationRepository = annotationRepository;
    this.batchSize = batchSize;
  }

  @Override
  public void start(XMLParserState state) {
    super.start(state);
    annotationBatch.set(new LinkedHashMap<Annotation, Map<QName, String>>());
  }

  @Override
  public void end(XMLParserState state) {
    final Map<Annotation, Map<QName, String>> batch = annotationBatch.get();
    if (!batch.isEmpty()) {
      emit(batch);
    }

    annotationBatch.remove();
    super.end(state);
  }

  protected void add(Annotation annotation, Map<QName, String> attributes) {
    final Map<Annotation, Map<QName, String>> batch = annotationBatch.get();
    batch.put(annotation, attributes);

    if ((batch.size() % batchSize) == 0) {
      emit(batch);
    }
  }

  protected void emit(Map<Annotation, Map<QName, String>> batch) {
    final Iterator<Annotation> annotationIt = annotationRepository.create(batch.keySet()).iterator();
    final Iterator<Map<QName, String>> attributesIt = batch.values().iterator();

    final Map<Annotation, Map<QName, String>> attrBatch = Maps.newHashMapWithExpectedSize(batch.size());
    while (annotationIt.hasNext() && attributesIt.hasNext()) {
      attrBatch.put(annotationIt.next(), attributesIt.next());
    }
    annotationRepository.set(attrBatch);

    batch.clear();
  }

}
