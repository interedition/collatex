package eu.interedition.text.xml;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class LineElementXMLParserModule extends XMLParserModuleAdapter {

  @Override
  public void start(XMLEntity entity, XMLParserState state) {
    final boolean lineElement = state.configuration.isLineElement(entity);
    if (lineElement && state.textOffset > 0) {
      state.insert("\n", false);
    }
  }
}
