package eu.interedition.text.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import eu.interedition.text.QName;
import eu.interedition.text.xml.XMLEntity;
import eu.interedition.text.xml.XMLParserConfiguration;
import eu.interedition.text.xml.XMLParserModule;
import eu.interedition.text.xml.module.LineElementXMLParserModule;
import eu.interedition.text.xml.module.NotableCharacterXMLParserModule;

import java.util.List;
import java.util.Set;

public class SimpleXMLParserConfiguration implements XMLParserConfiguration {

  protected Set<QName> excluded = Sets.newHashSet();
  protected Set<QName> included = Sets.newHashSet();
  protected Set<QName> lineElements = Sets.newHashSet();
  protected Set<QName> containerElements = Sets.newHashSet();
  protected Set<QName> notableElements = Sets.newHashSet();
  protected char notableCharacter = '\u25CA';
  protected boolean compressingWhitespace = true;
  private int textBufferSize = 102400;
  private boolean removeLeadingWhitespace = true;
  protected List<XMLParserModule> modules = Lists.<XMLParserModule>newArrayList();

  public void addLineElement(QName lineElementName) {
    lineElements.add(lineElementName);
  }

  public boolean removeLineElement(QName lineElementName) {
    return lineElements.remove(lineElementName);
  }

  public boolean isLineElement(XMLEntity entity) {
    return lineElements.contains(entity.getName());
  }

  public void addContainerElement(QName containerElementName) {
    containerElements.add(containerElementName);
  }

  public boolean removeContainerElement(QName containerElementName) {
    return containerElements.remove(containerElementName);
  }

  public boolean isContainerElement(XMLEntity entity) {
    return containerElements.contains(entity.getName());
  }

  public void include(QName name) {
    included.add(name);
  }

  public void exclude(QName name) {
    excluded.add(name);
  }

  public boolean included(XMLEntity entity) {
    return included.contains(entity.getName());
  }

  public boolean excluded(XMLEntity entity) {
    return excluded.contains(entity.getName());
  }

  public char getNotableCharacter() {
    return notableCharacter;
  }

  public void setNotableCharacter(char notableCharacter) {
    this.notableCharacter = notableCharacter;
  }

  public void addNotableElement(QName name) {
    notableElements.add(name);
  }

  public boolean removeNotableElement(QName name) {
    return notableElements.remove(name);
  }

  public boolean isNotable(XMLEntity entity) {
    return notableElements.contains(entity.getName());
  }

  public boolean isCompressingWhitespace() {
    return compressingWhitespace;
  }

  public void setCompressingWhitespace(boolean compressingWhitespace) {
    this.compressingWhitespace = compressingWhitespace;
  }

  public List<XMLParserModule> getModules() {
    return modules;
  }

  public int getTextBufferSize() {
    return textBufferSize;
  }

  public void setTextBufferSize(int textBufferSize) {
    this.textBufferSize = textBufferSize;
  }

  public boolean isRemoveLeadingWhitespace() {
    return removeLeadingWhitespace;
  }

  public void setRemoveLeadingWhitespace(boolean removeLeadingWhitespace) {
    this.removeLeadingWhitespace = removeLeadingWhitespace;
  }
}
