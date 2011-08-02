package eu.interedition.text.xml.module;

import eu.interedition.text.*;
import eu.interedition.text.xml.XMLEntity;
import eu.interedition.text.xml.XMLParserState;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Stack;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class AnnotationStorageXMLParserModule extends XMLParserModuleAdapter {

  private final AnnotationRepository annotationRepository;
  private final AnnotationDataRepository annotationDataRepository;

  private final ThreadLocal<Stack<Integer>> startOffsetStack = new ThreadLocal<Stack<Integer>>();
  private final ThreadLocal<Stack<Map<QName, String>>> attributeStack = new ThreadLocal<Stack<Map<QName, String>>>();

  public AnnotationStorageXMLParserModule(AnnotationRepository annotationRepository, AnnotationDataRepository annotationDataRepository) {
    this.annotationRepository = annotationRepository;
    this.annotationDataRepository = annotationDataRepository;
  }

  @Override
  public void start(XMLParserState state) {
    startOffsetStack.set(new Stack<Integer>());
    attributeStack.set(new Stack<Map<QName, String>>());
  }

  @Override
  public void end(XMLParserState state) {
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
    final Range range = new Range(startOffsetStack.get().pop(), state.getTextOffset());
    final Annotation annotation = annotationRepository.create(state.getTarget(), entity.getName(), range);
    annotationDataRepository.set(annotation, attributeStack.get().pop());
  }

}
