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
package eu.interedition.text.xml.module;

import com.google.common.base.Throwables;
import com.google.common.io.Closeables;
import eu.interedition.text.TextRepository;
import eu.interedition.text.xml.XMLEntity;
import eu.interedition.text.xml.XMLParserConfiguration;
import eu.interedition.text.xml.XMLParserState;

import java.io.IOException;
import java.io.Reader;
import java.util.Stack;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TextXMLParserModule extends XMLParserModuleAdapter {

  private final TextRepository textRepository;

  public TextXMLParserModule(TextRepository textRepository) {
    this.textRepository = textRepository;
  }

  @Override
  public void start(XMLEntity entity, XMLParserState state) {
    final XMLParserConfiguration configuration = state.getConfiguration();
    final Stack<Boolean> inclusionContext = state.getInclusionContext();

    final boolean parentIncluded = (inclusionContext.isEmpty() ? true : inclusionContext.peek());
    inclusionContext.push(parentIncluded ? !configuration.excluded(entity) : configuration.included(entity));
  }

  @Override
  public void end(XMLEntity entity, XMLParserState state) {
    state.getInclusionContext().pop();
  }

  @Override
  public void text(String text, XMLParserState state) {
    final Stack<Boolean> inclusionContext = state.getInclusionContext();
    if (!inclusionContext.isEmpty() && !inclusionContext.peek()) {
      return;
    }

    final Stack<Boolean> spacePreservationContext = state.getSpacePreservationContext();
    final Stack<XMLEntity> elementContext = state.getElementContext();

    final boolean preserveSpace = !spacePreservationContext.isEmpty() && spacePreservationContext.peek();
    if (!preserveSpace && !elementContext.isEmpty() && state.getConfiguration().isContainerElement(elementContext.peek())) {
      return;
    }

    state.insert(text, true);
  }

  @Override
  public void end(XMLParserState state) {
    try {
      Reader reader = null;
      try {
        textRepository.write(state.getTarget(), reader = state.readText());
      } finally {
        Closeables.close(reader, false);
      }
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }
}
