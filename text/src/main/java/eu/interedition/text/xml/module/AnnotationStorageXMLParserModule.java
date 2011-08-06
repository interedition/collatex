package eu.interedition.text.xml.module;

import com.google.common.collect.Maps;
import eu.interedition.text.*;
import eu.interedition.text.mem.SimpleAnnotation;
import eu.interedition.text.xml.XMLEntity;
import eu.interedition.text.xml.XMLParserState;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class AnnotationStorageXMLParserModule extends XMLParserModuleAdapter {

  private final AnnotationRepository annotationRepository;
  private final int batchSize;

  private final ThreadLocal<Stack<Integer>> startOffsetStack = new ThreadLocal<Stack<Integer>>();
  private final ThreadLocal<Stack<Map<QName, String>>> attributeStack = new ThreadLocal<Stack<Map<QName, String>>>();
  private final ThreadLocal<Map<Annotation, Map<QName, String>>> annotationBatch = new ThreadLocal<Map<Annotation, Map<QName, String>>>();


  public AnnotationStorageXMLParserModule(AnnotationRepository annotationRepository) {
    this(annotationRepository, 10000);
  }

  public AnnotationStorageXMLParserModule(AnnotationRepository annotationRepository, int batchSize) {
    this.annotationRepository = annotationRepository;
    this.batchSize = batchSize;
  }

  @Override
  public void start(XMLParserState state) {
    startOffsetStack.set(new Stack<Integer>());
    attributeStack.set(new Stack<Map<QName, String>>());
    annotationBatch.set(new LinkedHashMap<Annotation, Map<QName, String>>());
  }

  @Override
  public void end(XMLParserState state) {
    final Map<Annotation, Map<QName, String>> batch = annotationBatch.get();
    if (!batch.isEmpty()) {
      emit(batch);
    }

    annotationBatch.remove();
    attributeStack.remove();
    startOffsetStack.remove();
  }

  @Override
  public void start(XMLEntity entity, XMLParserState state) {
    startOffsetStack.get().push(state.getTextOffset());
    attributeStack.get().push(entity.getAttributes());
  }

  @Override
  public void end(XMLEntity entity, XMLParserState state) {
    final Map<Annotation, Map<QName, String>> batch = annotationBatch.get();

    final Range range = new Range(startOffsetStack.get().pop(), state.getTextOffset());
    batch.put(new SimpleAnnotation(state.getTarget(), entity.getName(), range), attributeStack.get().pop());

    if ((batch.size() % batchSize) == 0) {
      emit(batch);
    }
  }

  protected void emit(Map<Annotation, Map<QName, String>> batch) {
    final Map<Annotation, Map<QName, String>> created = Maps.newHashMapWithExpectedSize(batchSize);
    final Iterable<Annotation> createdAnnotations = annotationRepository.create(batch.keySet());

    final Iterator<Annotation> annotationIt = createdAnnotations.iterator();
    final Iterator<Map<QName, String>> attributesIt = batch.values().iterator();

    while (annotationIt.hasNext() && attributesIt.hasNext()) {
      created.put(annotationIt.next(), attributesIt.next());
    }

    annotationRepository.set(created);
    batch.clear();
  }
}
