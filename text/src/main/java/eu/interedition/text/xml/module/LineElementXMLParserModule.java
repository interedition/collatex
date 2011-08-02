package eu.interedition.text.xml.module;

import eu.interedition.text.xml.XMLEntity;
import eu.interedition.text.xml.XMLParserState;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class LineElementXMLParserModule extends XMLParserModuleAdapter {

  @Override
  public void start(XMLEntity entity, XMLParserState state) {
    final boolean lineElement = state.getConfiguration().isLineElement(entity);
    if (lineElement && state.getTextOffset() > 0) {
      state.insert("\n", false);
    }
  }
}
