package eu.interedition.text.xml;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import eu.interedition.text.Annotation;
import eu.interedition.text.QName;
import eu.interedition.text.Text;
import eu.interedition.text.TextRepository;
import eu.interedition.text.mem.SimpleQName;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.List;
import java.util.Map;

import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI;

public class XMLParser {
  private final TransformerFactory transformerFactory;
  private final XMLInputFactory xmlInputFactory;

  private TextRepository textRepository;

  public XMLParser() {
    transformerFactory = TransformerFactory.newInstance();
    xmlInputFactory = XMLInputFactory.newInstance();
    xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
    xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
    xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
  }

  public void setTextRepository(TextRepository textRepository) {
    this.textRepository = textRepository;
  }

  public Text load(Source xml) throws IOException, TransformerException {
    final File xmlSource = File.createTempFile(getClass().getName(), ".xml");
    Reader xmlSourceReader = null;
    try {

      final Transformer serializer = transformerFactory.newTransformer();
      serializer.setOutputProperty(OutputKeys.METHOD, "xml");
      serializer.setOutputProperty(OutputKeys.ENCODING, Text.CHARSET.name());
      serializer.setOutputProperty(OutputKeys.INDENT, "no");
      serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      serializer.transform(xml, new StreamResult(xmlSource));

      final Text text = textRepository.create(Text.Type.XML);
      xmlSourceReader = new InputStreamReader(new FileInputStream(xmlSource), Text.CHARSET);
      textRepository.write(text, xmlSourceReader);
      return text;
    } finally {
      Closeables.close(xmlSourceReader, false);
      xmlSource.delete();
    }
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
            while (reader.hasNext()) {
              final int event = reader.next();
              final int sourceOffset = reader.getLocation().getCharacterOffset();

              switch (event) {
                case XMLStreamConstants.START_DOCUMENT:
                  state.start();
                  break;
                case XMLStreamConstants.START_ELEMENT:
                  state.endText();
                  state.nextSibling();
                  state.start(XMLEntity.newElement(reader));
                  break;
                case XMLStreamConstants.END_ELEMENT:
                  state.endText();
                  state.end(new XMLEntity(new SimpleQName(reader.getName())));
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
                case XMLStreamConstants.END_DOCUMENT:
                  state.end();
                  break;
              }
            }

            state.writeText(textRepository);

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
