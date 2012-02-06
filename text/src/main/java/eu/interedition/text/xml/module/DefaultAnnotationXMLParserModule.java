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

import eu.interedition.text.Range;
import eu.interedition.text.TextRepository;
import eu.interedition.text.xml.XMLEntity;
import eu.interedition.text.xml.XMLParserState;

import java.util.Stack;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class DefaultAnnotationXMLParserModule extends AbstractAnnotationXMLParserModule {
  private Stack<Long> startOffsetStack;

  public DefaultAnnotationXMLParserModule(TextRepository textRepository, int batchSize, boolean addNodePath) {
    super(textRepository, batchSize, addNodePath);
  }

  public DefaultAnnotationXMLParserModule(TextRepository textRepository, int batchSize) {
    this(textRepository, batchSize, false);
  }

  @Override
  public void start(XMLParserState state) {
    super.start(state);
    startOffsetStack = new Stack<Long>();
  }

  @Override
  public void end(XMLParserState state) {
    startOffsetStack = null;
    super.end(state);
  }

  @Override
  public void start(XMLEntity entity, XMLParserState state) {
    super.start(entity, state);
    if (state.getInclusionContext().peek()) {
      startOffsetStack.push(state.getTextOffset());
    }
  }

  @Override
  public void end(XMLEntity entity, XMLParserState state) {
    if (state.getInclusionContext().peek()) {
      final Range range = new Range(startOffsetStack.pop(), state.getTextOffset());
      add(state, state.getTarget(), entity.getName(), range, entity.getAttributes());
    }
    super.end(entity, state);
  }
}
