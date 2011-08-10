package eu.interedition.text.xml.module;

import eu.interedition.text.AnnotationRepository;
import eu.interedition.text.QName;
import eu.interedition.text.Range;
import eu.interedition.text.mem.SimpleAnnotation;
import eu.interedition.text.xml.XMLEntity;
import eu.interedition.text.xml.XMLParserState;

import java.util.Map;
import java.util.Stack;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class DefaultAnnotationXMLParserModule extends AbstractAnnotationXMLParserModule {

  private final ThreadLocal<Stack<Integer>> startOffsetStack = new ThreadLocal<Stack<Integer>>();
  private final ThreadLocal<Stack<Map<QName, String>>> attributeStack = new ThreadLocal<Stack<Map<QName, String>>>();


  public DefaultAnnotationXMLParserModule(AnnotationRepository annotationRepository, int batchSize) {
    super(annotationRepository, batchSize);
  }

  @Override
  public void start(XMLParserState state) {
    super.start(state);
    startOffsetStack.set(new Stack<Integer>());
    attributeStack.set(new Stack<Map<QName, String>>());
  }

  @Override
  public void end(XMLParserState state) {
    attributeStack.remove();
    startOffsetStack.remove();
    super.end(state);
  }

  @Override
  public void start(XMLEntity entity, XMLParserState state) {
    super.start(entity, state);
    startOffsetStack.get().push(state.getTextOffset());
    attributeStack.get().push(entity.getAttributes());
  }

  @Override
  public void end(XMLEntity entity, XMLParserState state) {
    final Range range = new Range(startOffsetStack.get().pop(), state.getTextOffset());
    add(new SimpleAnnotation(state.getTarget(), entity.getName(), range), attributeStack.get().pop());
    super.end(entity, state);
  }
}
