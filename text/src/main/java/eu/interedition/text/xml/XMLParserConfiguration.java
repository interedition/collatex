package eu.interedition.text.xml;

import java.util.List;

public interface XMLParserConfiguration {

  boolean isLineElement(XMLEntity entity);

  boolean isContainerElement(XMLEntity entity);

  boolean included(XMLEntity entity);

  boolean excluded(XMLEntity entity);

  char getNotableCharacter();

  boolean isNotable(XMLEntity entity);

  boolean isCompressingWhitespace();

  List<XMLParserModule> getModules();

  int getTextBufferSize();

  boolean isRemoveLeadingWhitespace();
}