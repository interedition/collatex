package eu.interedition.text.xml;

import eu.interedition.text.mem.SimpleQName;

import java.util.Stack;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TextXMLParserModule extends XMLParserModuleAdapter {

  final Stack<Boolean> inclusionContext = new Stack<Boolean>();

  @Override
  public void start(XMLEntity entity, XMLParserState state) {
    final boolean parentIncluded = (inclusionContext.isEmpty() ? true : inclusionContext.peek());
    inclusionContext.push(parentIncluded ? !state.configuration.excluded(entity) : state.configuration.included(entity));
  }

  @Override
  public void end(XMLEntity entity, XMLParserState state) {
    inclusionContext.pop();
  }

  @Override
  public void text(String text, XMLParserState state) {
    if (!inclusionContext.isEmpty() && !inclusionContext.peek()) {
      return;
    }

    final boolean preserveSpace = !state.spacePreservationContext.isEmpty() && state.spacePreservationContext.peek();
    if (!preserveSpace && !state.elementContext.isEmpty() && state.configuration.isContainerElement(state.elementContext.peek())) {
      return;
    }

    state.insert(text, true);
  }
}
