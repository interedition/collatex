/*
 * #%L
 * Text: A text model with range-based markup via standoff annotations.
 * %%
 * Copyright (C) 2010 - 2011 The Interedition Development Group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package eu.interedition.text.xml;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.io.Closeables;
import com.google.common.io.FileBackedOutputStream;
import eu.interedition.text.Annotation;
import eu.interedition.text.Text;
import eu.interedition.text.TextConstants;
import eu.interedition.text.TextRange;
import eu.interedition.text.TextTarget;
import org.codehaus.jackson.JsonNode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import static eu.interedition.text.TextConstants.XML_TRANSFORM_NAME;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class XMLTransformer {
  private static final Logger LOG = LoggerFactory.getLogger(XMLTransformer.class);
  private final XMLInputFactory xmlInputFactory = XML.createXMLInputFactory();
  private final SessionFactory sessionFactory;
  private final XMLTransformerConfiguration configuration;
  private final List<XMLTransformerModule> modules;

  private final Stack<XMLEntity> elementContext = new Stack<XMLEntity>();
  private final Stack<Boolean> inclusionContext = new Stack<Boolean>();
  private final Stack<Boolean> spacePreservationContext = new Stack<Boolean>();
  private final XMLNodePath nodePath = new XMLNodePath();

  private Text source;
  private Text target;
  private FileBackedOutputStream textBuffer;

  private long textStartOffset;
  private char lastChar;

  private long sourceOffset;
  private long textOffset;
  private TextRange sourceOffsetRange;
  private TextRange textOffsetRange;


  public XMLTransformer(SessionFactory sessionFactory, XMLTransformerConfiguration configuration) {
    this.sessionFactory = sessionFactory;
    this.configuration = configuration;
    this.modules = configuration.getModules();
  }

  public Text transform(Text source) throws IOException, XMLStreamException {
    Preconditions.checkArgument(source.getType() == Text.Type.XML);
    final Session session = sessionFactory.getCurrentSession();

    final Annotation layer = Iterables.getOnlyElement(Annotation.create(session, new Annotation(XML_TRANSFORM_NAME, new TextTarget(source, 0, source.getLength()), null)));

    this.source = source;
    this.target = Text.create(session, layer, Text.Type.TXT);

    try {
      Reader xmlReader = null;
      XMLStreamReader reader = null;
      try {
        xmlReader = source.read().getInput();
        reader = xmlInputFactory.createXMLStreamReader(xmlReader);

        final Stack<XMLEntity> entities = new Stack<XMLEntity>();
        start();
        while (reader.hasNext()) {
          final int event = reader.next();
          mapOffsetDelta(0, reader.getLocation().getCharacterOffset() - sourceOffset);

          switch (event) {
            case XMLStreamConstants.START_ELEMENT:
              endText();
              nextSibling();
              start(entities.push(XMLEntity.newElement(reader)));
              break;
            case XMLStreamConstants.END_ELEMENT:
              endText();
              end(entities.pop());
              break;
            case XMLStreamConstants.COMMENT:
              endText();
              nextSibling();
              emptyEntity(XMLEntity.newComment(reader));
              break;
            case XMLStreamConstants.PROCESSING_INSTRUCTION:
              endText();
              nextSibling();
              emptyEntity(XMLEntity.newPI(reader));
              break;
            case XMLStreamConstants.CHARACTERS:
            case XMLStreamConstants.ENTITY_REFERENCE:
            case XMLStreamConstants.CDATA:
              newText(reader.getText());
              break;
          }
        }
        end();
      } finally {
        XML.closeQuietly(reader);
        Closeables.close(xmlReader, false);
      }
      Reader textReader = null;
      try {
        return target.write(session, textReader = read());
      } finally {
        Closeables.close(textReader, false);
      }
    } catch (Throwable t) {
      Throwables.propagateIfInstanceOf(t, IOException.class);
      Throwables.propagateIfInstanceOf(Throwables.getRootCause(t), XMLStreamException.class);
      throw Throwables.propagate(t);
    }
  }

  public Text getSource() {
    return source;
  }

  public Text getTarget() {
    return target;
  }

  public SessionFactory getSessionFactory() {
    return sessionFactory;
  }

  public XMLTransformerConfiguration getConfiguration() {
    return configuration;
  }

  public List<XMLTransformerModule> getModules() {
    return Collections.unmodifiableList(modules);
  }

  public Stack<Boolean> getInclusionContext() {
    return inclusionContext;
  }

  public boolean isIncluded() {
    return inclusionContext.isEmpty() || inclusionContext.peek();
  }

  public Stack<Boolean> getSpacePreservationContext() {
    return spacePreservationContext;
  }

  public boolean isSpacePreserved() {
    return !spacePreservationContext.isEmpty() && spacePreservationContext.peek();
  }

  public Stack<XMLEntity> getElementContext() {
    return elementContext;
  }

  public boolean isContainerElement() {
    return !elementContext.isEmpty() && configuration.isContainerElement(elementContext.peek());
  }

  public boolean isLineElement() {
    return !elementContext.isEmpty() && configuration.isLineElement(elementContext.peek());
  }

  public boolean isNotable() {
    return !elementContext.isEmpty() && configuration.isNotable(elementContext.peek());
  }

  public XMLNodePath getNodePath() {
    return nodePath;
  }

  public long getTextOffset() {
    return textOffset;
  }

  public long getSourceOffset() {
    return sourceOffset;
  }

  public long getTextStartOffset() {
    return textStartOffset;
  }

  public void write(String text, boolean fromSource) {
    if (LOG.isTraceEnabled()) {
      LOG.trace("Inserting Text: '" + text.replaceAll("[\r\n]+", "\\\\n") + "' (" + (fromSource ? "from source" : "generated") + ")");
    }
    try {
      final int textLength = text.length();
      final StringBuilder inserted = new StringBuilder();
      if (fromSource) {
        final boolean preserveSpace = isSpacePreserved();
        for (int cc = 0; cc < textLength; cc++) {
          char currentChar = text.charAt(cc);
          if (!preserveSpace && configuration.isCompressingWhitespace() && Character.isWhitespace(lastChar) && Character.isWhitespace(currentChar)) {
            mapOffsetDelta(0, 1);
            continue;
          }
          if (currentChar == '\n' || currentChar == '\r') {
            currentChar = ' ';
          }
          textBuffer.write(Character.toString(lastChar = currentChar).getBytes(Text.CHARSET));
          inserted.append(lastChar);
          mapOffsetDelta(1, 1);
        }
      } else {
        textBuffer.write(text.getBytes(Text.CHARSET));
        inserted.append(text);
        mapOffsetDelta(inserted.length(), 0);
      }

      final String insertedStr = inserted.toString();
      for (XMLTransformerModule m : configuration.getModules()) {
        m.textWritten(this, text, insertedStr);
      }
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  Reader read() throws IOException {
    textBuffer.flush();
    return new InputStreamReader(textBuffer.getSupplier().getInput(), Text.CHARSET);
  }

  void start() {
    if (LOG.isTraceEnabled()) {
      LOG.trace("Start of document");
    }

    elementContext.clear();
    inclusionContext.clear();
    spacePreservationContext.clear();
    nodePath.clear();

    textBuffer = new FileBackedOutputStream(configuration.getTextBufferSize(), true);
    textStartOffset = -1;
    lastChar = (configuration.isRemoveLeadingWhitespace() ? ' ' : 0);

    sourceOffset = 0;
    textOffset = 0;

    sourceOffsetRange = TextRange.NULL;
    textOffsetRange = TextRange.NULL;

    this.nodePath.push(0);
    for (XMLTransformerModule m : modules) {
      m.start(this);
    }
  }

  void end() {
    emitOffsetMapping();
    if (LOG.isTraceEnabled()) {
      LOG.trace("End of document");
    }
    for (XMLTransformerModule m : modules) {
      m.end(this);
    }
    this.nodePath.pop();
  }

  void start(XMLEntity entity) {
    if (LOG.isTraceEnabled()) {
      LOG.trace("Start of " + entity);
    }

    final boolean parentIncluded = (inclusionContext.isEmpty() ? true : inclusionContext.peek());
    inclusionContext.push(parentIncluded ? !configuration.excluded(entity) : configuration.included(entity));

    spacePreservationContext.push(spacePreservationContext.isEmpty() ? false : spacePreservationContext.peek());
    final JsonNode xmlSpace = entity.getAttributes().get(TextConstants.XML_SPACE_ATTR_NAME.toString());
    if (xmlSpace != null) {
      spacePreservationContext.pop();
      spacePreservationContext.push("preserve".equalsIgnoreCase(xmlSpace.toString()));
    }

    nodePath.set(entity.getAttributes());
    nodePath.push(0);

    elementContext.push(entity);

    for (XMLTransformerModule m : modules) {
      m.start(this, entity);
    }
  }

  void end(XMLEntity entity) {
    if (LOG.isTraceEnabled()) {
      LOG.trace("End of " + entity);
    }

    for (XMLTransformerModule m : modules) {
      m.end(this, entity);
    }

    elementContext.pop();
    nodePath.pop();
    spacePreservationContext.pop();
    inclusionContext.pop();
  }

  void emptyEntity(XMLEntity entity) {
    start(entity);
    end(entity);
  }

  void nextSibling() {
    if (LOG.isTraceEnabled()) {
      LOG.trace("Next sibling");
    }

    nodePath.push(nodePath.pop() + 1);
  }

  void endText() {
    if (textStartOffset >= 0 && textOffset > textStartOffset) {
      if (LOG.isTraceEnabled()) {
        LOG.trace("End of text node");
      }
      for (XMLTransformerModule m : modules) {
        m.endText(this);
      }
    }
    textStartOffset = -1;
  }

  void newText(String text) throws IOException {
    if (textStartOffset < 0) {
      nextSibling();
      textStartOffset = textOffset;

      if (LOG.isTraceEnabled()) {
        LOG.trace("Start of text node");
      }
      for (XMLTransformerModule m : modules) {
        m.startText(this);
      }
    }

    if (LOG.isTraceEnabled()) {
      LOG.trace("Text: '" + text.replaceAll("[\r\n]+", "\\\\n") + "'");
    }
    for (XMLTransformerModule m : modules) {
      m.text(this, text);
    }
  }

  void mapOffsetDelta(long addToText, long addToSource) {
    if (addToText == 0 && addToSource == 0) {
      return;
    }

    if (LOG.isTraceEnabled()) {
      LOG.trace("Moving offsets: text += " + addToText + "; source += " + addToSource);
    }

    final long textOffsetRangeLength = textOffsetRange.length();
    final long sourceOffsetRangeLength = sourceOffsetRange.length();

    if (addToText == 0 && textOffsetRangeLength == 0) {
      sourceOffsetRange = new TextRange(sourceOffsetRange.getStart(), sourceOffsetRange.getEnd() + addToSource);
    } else if (addToSource == 0 && sourceOffsetRangeLength == 0) {
      textOffsetRange = new TextRange(textOffsetRange.getStart(), textOffsetRange.getEnd() + addToText);
    } else if (textOffsetRangeLength == sourceOffsetRangeLength && addToText == addToSource) {
      sourceOffsetRange = new TextRange(sourceOffsetRange.getStart(), sourceOffsetRange.getEnd() + addToSource);
      textOffsetRange = new TextRange(textOffsetRange.getStart(), textOffsetRange.getEnd() + addToText);
    } else {
      emitOffsetMapping();
      sourceOffsetRange = new TextRange(sourceOffsetRange.getEnd(), sourceOffsetRange.getEnd() + addToSource);
      textOffsetRange = new TextRange(textOffsetRange.getEnd(), textOffsetRange.getEnd() + addToText);
    }

    this.textOffset += addToText;
    this.sourceOffset += addToSource;
  }

  void emitOffsetMapping() {
    if (textOffsetRange.length() == 0 && sourceOffsetRange.length() == 0) {
      return;
    }

    if (LOG.isTraceEnabled()) {
      LOG.trace("New offset mapping: text = " + textOffsetRange + "==> source += " + sourceOffsetRange);
    }
    for (XMLTransformerModule m : modules) {
      m.offsetMapping(this, textOffsetRange, sourceOffsetRange);
    }
  }
}
