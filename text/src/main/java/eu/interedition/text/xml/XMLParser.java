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
import com.google.common.io.Closeables;
import eu.interedition.text.Text;
import eu.interedition.text.TextConsumer;
import eu.interedition.text.TextRepository;

import javax.xml.stream.*;
import java.io.IOException;
import java.io.Reader;
import java.util.Stack;

public class XMLParser {
  private final XMLInputFactory xmlInputFactory = XML.createXMLInputFactory();

  private TextRepository textRepository;

  public void setTextRepository(TextRepository textRepository) {
    this.textRepository = textRepository;
  }

  public Text parse(Text source, final XMLParserConfiguration configuration) throws IOException, XMLStreamException {
    Preconditions.checkArgument(source.getType() == Text.Type.XML);
    final Text target = textRepository.create(null, Text.Type.TXT);
    final XMLParserState state = new XMLParserState(source, target, configuration);
    try {
      textRepository.read(source, new XMLParserTextConsumer(state));
      Reader textReader = null;
      try {
        return textRepository.write(state.getTarget(), textReader = state.readText());
      } finally {
        Closeables.close(textReader, false);
      }
    } catch (Throwable t) {
      Throwables.propagateIfInstanceOf(t, IOException.class);
      Throwables.propagateIfInstanceOf(Throwables.getRootCause(t), XMLStreamException.class);
      throw Throwables.propagate(t);
    }
  }

  private class XMLParserTextConsumer implements TextConsumer {
    private final XMLParserState state;

    public XMLParserTextConsumer(XMLParserState state) {
      this.state = state;
    }

    public void read(Reader content, long contentLength) throws IOException {
      XMLStreamReader reader = null;
      final Stack<XMLEntity> entities = new Stack<XMLEntity>();
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
              state.start(entities.push(XMLEntity.newElement(reader)));
              break;
            case XMLStreamConstants.END_ELEMENT:
              state.endText();
              state.end(entities.pop());
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
  }
}
