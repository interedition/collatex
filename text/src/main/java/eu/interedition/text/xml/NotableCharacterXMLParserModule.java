package eu.interedition.text.xml;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class NotableCharacterXMLParserModule extends XMLParserModuleAdapter {
  @Override
  public void start(XMLEntity entity, XMLParserState state) {
    if (state.configuration.isNotable(entity.getName())) {
      state.insert(Character.toString(state.configuration.getNotableCharacter()), false);
    }
  }
}
