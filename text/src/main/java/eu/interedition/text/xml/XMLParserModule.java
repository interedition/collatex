package eu.interedition.text.xml;

import eu.interedition.text.Range;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface XMLParserModule {
  void start(XMLParserState state);

  void start(XMLEntity entity, XMLParserState state);

  void startText(XMLParserState state);

  void text(String text, XMLParserState state);

  void endText(XMLParserState state);

  void end(XMLEntity entity, XMLParserState state);

  void insertText(String read, String inserted, XMLParserState state);

  void end(XMLParserState state);

  void offsetMapping(XMLParserState state, Range textRange, Range sourceRange);
}
