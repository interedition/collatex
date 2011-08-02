package eu.interedition.text.xml.module;

import com.google.common.base.Throwables;
import com.google.common.io.Closeables;
import eu.interedition.text.TextRepository;
import eu.interedition.text.xml.XMLEntity;
import eu.interedition.text.xml.XMLParserConfiguration;
import eu.interedition.text.xml.XMLParserState;

import java.io.IOException;
import java.io.Reader;
import java.util.Stack;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TextXMLParserModule extends XMLParserModuleAdapter {

  private final TextRepository textRepository;

  public TextXMLParserModule(TextRepository textRepository) {
    this.textRepository = textRepository;
  }

  @Override
  public void start(XMLEntity entity, XMLParserState state) {
    final XMLParserConfiguration configuration = state.getConfiguration();
    final Stack<Boolean> inclusionContext = state.getInclusionContext();

    final boolean parentIncluded = (inclusionContext.isEmpty() ? true : inclusionContext.peek());
    inclusionContext.push(parentIncluded ? !configuration.excluded(entity) : configuration.included(entity));
  }

  @Override
  public void end(XMLEntity entity, XMLParserState state) {
    state.getInclusionContext().pop();
  }

  @Override
  public void text(String text, XMLParserState state) {
    final Stack<Boolean> inclusionContext = state.getInclusionContext();
    if (!inclusionContext.isEmpty() && !inclusionContext.peek()) {
      return;
    }

    final Stack<Boolean> spacePreservationContext = state.getSpacePreservationContext();
    final Stack<XMLEntity> elementContext = state.getElementContext();

    final boolean preserveSpace = !spacePreservationContext.isEmpty() && spacePreservationContext.peek();
    if (!preserveSpace && !elementContext.isEmpty() && state.getConfiguration().isContainerElement(elementContext.peek())) {
      return;
    }

    state.insert(text, true);
  }

  @Override
  public void end(XMLParserState state) {
    try {
      Reader reader = null;
      try {
        textRepository.write(state.getTarget(), reader = state.readText());
      } finally {
        Closeables.close(reader, false);
      }
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }
}
