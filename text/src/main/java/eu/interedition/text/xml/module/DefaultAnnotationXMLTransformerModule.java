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

import eu.interedition.text.Annotation;
import eu.interedition.text.TextTarget;
import eu.interedition.text.xml.XMLEntity;
import eu.interedition.text.xml.XMLTransformer;

import java.util.Stack;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class DefaultAnnotationXMLTransformerModule extends AbstractAnnotationXMLTransformerModule {
  private Stack<Long> startOffsetStack;

  public DefaultAnnotationXMLTransformerModule(int batchSize, boolean addNodePath) {
    super(batchSize, addNodePath);
  }

  public DefaultAnnotationXMLTransformerModule(int batchSize) {
    this(batchSize, false);
  }

  @Override
  public void start(XMLTransformer transformer) {
    super.start(transformer);
    startOffsetStack = new Stack<Long>();
  }

  @Override
  public void end(XMLTransformer transformer) {
    startOffsetStack = null;
    super.end(transformer);
  }

  @Override
  public void start(XMLTransformer transformer, XMLEntity entity) {
    super.start(transformer, entity);
    if (transformer.getInclusionContext().peek()) {
      startOffsetStack.push(transformer.getTextOffset());
    }
  }

  @Override
  public void end(XMLTransformer transformer, XMLEntity entity) {
    if (transformer.getInclusionContext().peek()) {
      final TextTarget target = new TextTarget(transformer.getTarget(), startOffsetStack.pop(), transformer.getTextOffset());
      add(transformer, new Annotation(entity.getName(), target, entity.getAttributes()));
    }
    super.end(transformer, entity);
  }
}
