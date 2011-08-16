package eu.interedition.text.xml;

import com.google.common.base.Throwables;
import com.google.common.io.FileBackedOutputStream;
import eu.interedition.text.Range;
import eu.interedition.text.Text;
import eu.interedition.text.TextConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class XMLParserState {
  private static final Logger LOG = LoggerFactory.getLogger(XMLParserState.class);

  private final Text source;
  private final Text target;
  private final XMLParserConfiguration configuration;

  private final List<XMLParserModule> modules;

  private final Stack<XMLEntity> elementContext = new Stack<XMLEntity>();
  private final Stack<Boolean> inclusionContext = new Stack<Boolean>();
  private final Stack<Boolean> spacePreservationContext = new Stack<Boolean>();
  private final Stack<Integer> nodePath = new Stack<Integer>();

  private final FileBackedOutputStream textBuffer;

  private long textStartOffset = -1;
  private char lastChar;

  private long sourceOffset = 0;
  private long textOffset = 0;
  private Range sourceOffsetRange = Range.NULL;
  private Range textOffsetRange = Range.NULL;

  XMLParserState(Text source, Text target, XMLParserConfiguration configuration) {
    this.source = source;
    this.target = target;
    this.configuration = configuration;
    this.modules = configuration.getModules();
    this.textBuffer = new FileBackedOutputStream(configuration.getTextBufferSize(), true);
    this.lastChar = (configuration.isRemoveLeadingWhitespace() ? ' ' : 0);
  }

  public Text getSource() {
    return source;
  }

  public Text getTarget() {
    return target;
  }

  public XMLParserConfiguration getConfiguration() {
    return configuration;
  }

  public List<XMLParserModule> getModules() {
    return Collections.unmodifiableList(modules);
  }

  public Stack<Boolean> getInclusionContext() {
    return inclusionContext;
  }

  public Stack<Boolean> getSpacePreservationContext() {
    return spacePreservationContext;
  }

  public Stack<XMLEntity> getElementContext() {
    return elementContext;
  }

  public Stack<Integer> getNodePath() {
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

  public void insert(String text, boolean fromSource) {
    if (LOG.isTraceEnabled()) {
      LOG.trace("Inserting Text: '" + text.replaceAll("[\r\n]+", "\\\\n") + "' (" + (fromSource ? "from source" : "generated") + ")");
    }
    try {
      final int textLength = text.length();
      final StringBuilder inserted = new StringBuilder();
      if (fromSource) {
        final boolean preserveSpace = !spacePreservationContext.isEmpty() && spacePreservationContext.peek();
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
      for (XMLParserModule m : configuration.getModules()) {
        m.insertText(text, insertedStr, this);
      }
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  public Reader readText() throws IOException {
    textBuffer.flush();
    return new InputStreamReader(textBuffer.getSupplier().getInput(), Text.CHARSET);
  }

  void start() {
    if (LOG.isTraceEnabled()) {
      LOG.trace("Start of document");
    }

    this.nodePath.push(0);
    for (XMLParserModule m : modules) {
      m.start(this);
    }
  }

  void end() {
    emitOffsetMapping();
    if (LOG.isTraceEnabled()) {
      LOG.trace("End of document");
    }
    for (XMLParserModule m : modules) {
      m.end(this);
    }
    this.nodePath.pop();
    try {
      textBuffer.reset();
    } catch (IOException e) {
    }
  }

  void start(XMLEntity entity) {
    if (LOG.isTraceEnabled()) {
      LOG.trace("Start of " + entity);
    }
    for (XMLParserModule m : modules) {
      m.start(entity, this);
    }

    spacePreservationContext.push(spacePreservationContext.isEmpty() ? false : spacePreservationContext.peek());
    final String xmlSpace = entity.getAttributes().get(TextConstants.XML_SPACE_ATTR_NAME);
    if (xmlSpace != null) {
      spacePreservationContext.pop();
      spacePreservationContext.push("preserve".equalsIgnoreCase(xmlSpace));
    }

    nodePath.push(0);
    elementContext.push(entity);
  }

  void end(XMLEntity entity) {
    if (LOG.isTraceEnabled()) {
      LOG.trace("End of " + entity);
    }
    elementContext.pop();
    nodePath.pop();
    spacePreservationContext.pop();

    for (XMLParserModule m : modules) {
      m.end(entity, this);
    }
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
      for (XMLParserModule m : modules) {
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
      for (XMLParserModule m : modules) {
        m.startText(this);
      }
    }

    if (LOG.isTraceEnabled()) {
      LOG.trace("Text: '" + text.replaceAll("[\r\n]+", "\\\\n") + "'");
    }
    for (XMLParserModule m : modules) {
      m.text(text, this);
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
      sourceOffsetRange = new Range(sourceOffsetRange.getStart(), sourceOffsetRange.getEnd() + addToSource);
    } else if (addToSource == 0 && sourceOffsetRangeLength == 0) {
      textOffsetRange = new Range(textOffsetRange.getStart(), textOffsetRange.getEnd() + addToText);
    } else if (textOffsetRangeLength == sourceOffsetRangeLength && addToText == addToSource) {
      sourceOffsetRange = new Range(sourceOffsetRange.getStart(), sourceOffsetRange.getEnd() + addToSource);
      textOffsetRange = new Range(textOffsetRange.getStart(), textOffsetRange.getEnd() + addToText);
    } else {
      emitOffsetMapping();
      sourceOffsetRange = new Range(sourceOffsetRange.getEnd(), sourceOffsetRange.getEnd() + addToSource);
      textOffsetRange = new Range(textOffsetRange.getEnd(), textOffsetRange.getEnd() + addToText);
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
    for (XMLParserModule m : modules) {
      m.offsetMapping(this, textOffsetRange, sourceOffsetRange);
    }
  }
}
