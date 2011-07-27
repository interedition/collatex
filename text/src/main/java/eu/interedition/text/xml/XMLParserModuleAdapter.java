package eu.interedition.text.xml;

import eu.interedition.text.Range;

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

  public void newOffsetDelta(XMLParserState state, Range textRange, Range sourceRange) {
  }

  public void endText(XMLParserState state) {
  }
}
