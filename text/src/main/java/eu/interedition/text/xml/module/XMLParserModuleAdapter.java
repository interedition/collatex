package eu.interedition.text.xml.module;

import eu.interedition.text.Range;
import eu.interedition.text.xml.XMLEntity;
import eu.interedition.text.xml.XMLParserModule;
import eu.interedition.text.xml.XMLParserState;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class XMLParserModuleAdapter implements XMLParserModule {
  public void start(XMLParserState state) {
  }

  public void start(XMLEntity entity, XMLParserState state) {
  }

  public void startText(XMLParserState state) {
  }

  public void end(XMLEntity entity, XMLParserState state) {
  }

  public void text(String text, XMLParserState state) {
  }

  public void insertText(String read, String inserted, XMLParserState state) {
  }

  public void end(XMLParserState state) {
  }

  public void offsetMapping(XMLParserState state, Range textRange, Range sourceRange) {
  }

  public void endText(XMLParserState state) {
  }
}
