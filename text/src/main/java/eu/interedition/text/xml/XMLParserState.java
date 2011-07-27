package eu.interedition.text.xml;

import com.google.common.base.Throwables;
import com.google.common.io.Closeables;
import com.google.common.io.FileBackedOutputStream;
import eu.interedition.text.Range;
import eu.interedition.text.Text;
import eu.interedition.text.TextRepository;
import eu.interedition.text.mem.SimpleQName;

import javax.xml.stream.XMLStreamReader;
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
  public final Text source;
  public final Text target;
  public final XMLParserConfiguration configuration;

  List<XMLParserModule> modules;

  final Stack<XMLEntity> elementContext = new Stack<XMLEntity>();
  final Stack<Boolean> spacePreservationContext = new Stack<Boolean>();
  final Stack<Integer> nodePath = new Stack<Integer>();

  final FileBackedOutputStream textBuffer;

  int textOffset = 0;

  int textStartOffset = -1;
  char lastChar;

  Range lastDeltaTextRange = Range.NULL;
  Range lastDeltaSourceRange = Range.NULL;

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
    for (XMLParserModule m : modules) {
      m.start(this);
    }
  }

  void end() {
    for (XMLParserModule m : modules) {
      m.end(this);
    }
  }

  void start(XMLEntity entity) {
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
    nodePath.push(nodePath.pop() + 1);
  }

  void endText() {
    if (textStartOffset >= 0 && textOffset > textStartOffset) {
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

      for (XMLParserModule m : modules) {
        m.startText(this);
      }
    }

    for (XMLParserModule m : modules) {
      m.text(text, this);
    }
  }

  void insert(String text, boolean fromSource) {
    try {
      final int textLength = text.length();
      final StringBuilder inserted = new StringBuilder();
      if (fromSource) {
        final boolean preserveSpace = !spacePreservationContext.isEmpty() && spacePreservationContext.peek();
        for (int cc = 0; cc < textLength; cc++) {
          char currentChar = text.charAt(cc);
          if (!preserveSpace && configuration.isCompressingWhitespace() && Character.isWhitespace(lastChar) && Character.isWhitespace(currentChar)) {
            // FIXME: does this influence the offset mapping?
            continue;
          }
          if (currentChar == '\n' || currentChar == '\r') {
            currentChar = ' ';
          }
          textBuffer.write(Character.toString(lastChar = currentChar).getBytes(Text.CHARSET));
          inserted.append(lastChar);
          textOffset++;
        }
      } else {
        textBuffer.write(text.getBytes(Text.CHARSET));
        inserted.append(text);
        textOffset += textLength;
        syncOffsetsUpTo(textOffset, lastDeltaSourceRange.getEnd());
      }

      final String insertedStr = inserted.toString();
      for (XMLParserModule m : configuration.getModules()) {
        m.insertText(text, insertedStr, this);
      }
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  protected void syncOffsetsUpTo(int textOffset, int sourceOffset) {
    if (lastDeltaSourceRange.getEnd() < sourceOffset || lastDeltaTextRange.getEnd() < textOffset) {
      lastDeltaTextRange = new Range(lastDeltaTextRange.getEnd(), textOffset);
      lastDeltaSourceRange = new Range(lastDeltaSourceRange.getEnd(), sourceOffset);
      for (XMLParserModule m : modules) {
        m.newOffsetDelta(this, lastDeltaTextRange, lastDeltaSourceRange);
      }
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
}
