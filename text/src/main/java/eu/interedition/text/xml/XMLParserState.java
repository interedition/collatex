package eu.interedition.text.xml;

import com.google.common.base.Throwables;
import com.google.common.io.Closeables;
import com.google.common.io.FileBackedOutputStream;
import eu.interedition.text.Range;
import eu.interedition.text.Text;
import eu.interedition.text.TextRepository;
import eu.interedition.text.mem.SimpleQName;
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

  public final Text source;
  public final Text target;
  public final XMLParserConfiguration configuration;

  List<XMLParserModule> modules;

  final Stack<XMLEntity> elementContext = new Stack<XMLEntity>();
  final Stack<Boolean> spacePreservationContext = new Stack<Boolean>();
  final Stack<Integer> nodePath = new Stack<Integer>();

  final FileBackedOutputStream textBuffer;

  int textStartOffset = -1;
  char lastChar;

  int sourceOffset = 0;
  int textOffset = 0;
  Range sourceOffsetRange = Range.NULL;
  Range textOffsetRange = Range.NULL;

  XMLParserState(Text source, Text target, XMLParserConfiguration configuration) {
    this.source = source;
    this.target = target;
    this.configuration = configuration;
    this.modules = configuration.getModules();
    this.nodePath.push(0);
    this.textBuffer = new FileBackedOutputStream(configuration.getTextBufferSize());
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

  public Stack<XMLEntity> getElementContext() {
    return elementContext;
  }

  public Stack<Integer> getNodePath() {
    return nodePath;
  }

  public int getTextOffset() {
    return textOffset;
  }

  public int getTextStartOffset() {
    return textStartOffset;
  }

  void start() {
    if (LOG.isTraceEnabled()) {
      LOG.trace("Start of document");
    }

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
  }

  void start(XMLEntity entity) {
    if (LOG.isTraceEnabled()) {
      LOG.trace("Start of " + entity);
    }
    for (XMLParserModule m : modules) {
      m.start(entity, this);
    }

    spacePreservationContext.push(spacePreservationContext.isEmpty() ? false : spacePreservationContext.peek());
    final String xmlSpace = entity.getAttributes().get(SimpleQName.XML_SPACE);
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

  void insert(String text, boolean fromSource) {
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

  void writeText(TextRepository textRepository) throws IOException {
    Reader textBufferReader = null;
    try {
      textRepository.write(target, textBufferReader = new InputStreamReader(textBuffer.getSupplier().getInput(), Text.CHARSET));
    } finally {
      Closeables.close(textBufferReader, false);
      textBuffer.reset();
    }
  }

  void mapOffsetDelta(int addToText, int addToSource) {
    if (addToText == 0 && addToSource == 0) {
      return;
    }

    if (LOG.isTraceEnabled()) {
      LOG.trace("Moving offsets: text += " + addToText + "; source += " + addToSource);
    }

    final int textOffsetRangeLength = textOffsetRange.length();
    final int sourceOffsetRangeLength = sourceOffsetRange.length();

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
