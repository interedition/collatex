package eu.interedition.text.xml.module;

import eu.interedition.text.xml.XMLEntity;
import eu.interedition.text.xml.XMLParserState;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class NotableCharacterXMLParserModule extends XMLParserModuleAdapter {
  @Override
  public void start(XMLEntity entity, XMLParserState state) {
    if (state.getConfiguration().isNotable(entity)) {
      state.insert(Character.toString(state.getConfiguration().getNotableCharacter()), false);
    }
  }
}
