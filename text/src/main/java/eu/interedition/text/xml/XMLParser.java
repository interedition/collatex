package eu.interedition.text.xml;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import eu.interedition.text.Text;
import eu.interedition.text.TextRepository;
import eu.interedition.text.mem.SimpleQName;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.Reader;

public class XMLParser {
  private final XMLInputFactory xmlInputFactory;

  private TextRepository textRepository;

  public XMLParser() {
    xmlInputFactory = XMLInputFactory.newInstance();
    xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
    xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
    xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
  }

  public void setTextRepository(TextRepository textRepository) {
    this.textRepository = textRepository;
  }

  public Text parse(Text source, final XMLParserConfiguration configuration)
          throws IOException, XMLStreamException {
    Preconditions.checkArgument(source.getType() == Text.Type.XML);
    final Text text = textRepository.create(Text.Type.PLAIN);

    final XMLParserState state = new XMLParserState(source, text, configuration);
    try {
      textRepository.read(source, new TextRepository.TextReader() {
        public void read(Reader content, int contentLength) throws IOException {
          XMLStreamReader reader = null;
          try {
            reader = xmlInputFactory.createXMLStreamReader(content);
            state.start();
            while (reader.hasNext()) {
              final int event = reader.next();
              state.mapOffsetDelta(0, reader.getLocation().getCharacterOffset() - state.getSourceOffset());

              switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                  state.endText();
                  state.nextSibling();
                  state.start(XMLEntity.newElement(reader));
                  break;
                case XMLStreamConstants.END_ELEMENT:
                  state.endText();
                  state.end(new XMLEntity(new SimpleQName(reader.getName()), reader.getName().getPrefix()));
                  break;
                case XMLStreamConstants.COMMENT:
                  state.endText();
                  state.nextSibling();
                  state.emptyEntity(XMLEntity.newComment(reader));
                  break;
                case XMLStreamConstants.PROCESSING_INSTRUCTION:
                  state.endText();
                  state.nextSibling();
                  state.emptyEntity(XMLEntity.newPI(reader));
                  break;
                case XMLStreamConstants.CHARACTERS:
                case XMLStreamConstants.ENTITY_REFERENCE:
                case XMLStreamConstants.CDATA:
                  state.newText(reader.getText());
                  break;
              }
            }
            state.end();

          } catch (XMLStreamException e) {
            throw Throwables.propagate(e);
          } finally {
            if (reader != null) {
              try {
                reader.close();
              } catch (XMLStreamException e) {
              }
            }
          }
        }
      });
    } catch (Throwable t) {
      Throwables.propagateIfInstanceOf(t, IOException.class);
      Throwables.propagateIfInstanceOf(Throwables.getRootCause(t), XMLStreamException.class);
      throw Throwables.propagate(t);
    }

    return text;
  }
}
