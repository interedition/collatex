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

import com.google.common.collect.Lists;
import eu.interedition.text.Annotation;
import eu.interedition.text.TextConstants;
import eu.interedition.text.xml.XMLTransformer;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.hibernate.Session;

import java.util.List;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public abstract class AbstractAnnotationXMLTransformerModule extends XMLTransformerModuleAdapter {
  private static final String XML_NODE_ATTR = TextConstants.XML_NODE_ATTR_NAME.toString();

  protected final int batchSize;
  protected final boolean addNodePath;

  private List<Annotation> annotationBatch;

  protected AbstractAnnotationXMLTransformerModule(int batchSize, boolean addNodePath) {
    this.batchSize = batchSize;
    this.addNodePath = addNodePath;
  }

  @Override
  public void start(XMLTransformer transformer) {
    super.start(transformer);
    annotationBatch = Lists.newArrayList();
  }

  @Override
  public void end(XMLTransformer transformer) {
    if (!annotationBatch.isEmpty()) {
      emit(transformer.getSession());
    }

    annotationBatch = null;
    super.end(transformer);
  }

  protected void add(XMLTransformer transformer, Annotation annotation) {
    final JsonNode data = annotation.getData();
    if (!addNodePath && data.isObject()) {
      ((ObjectNode) data).remove(XML_NODE_ATTR);
    }

    annotationBatch.add(annotation);

    if ((annotationBatch.size() % batchSize) == 0) {
      emit(transformer.getSession());
    }
  }

  protected void emit(Session session) {
    Annotation.create(session, annotationBatch);
    annotationBatch.clear();
  }

}
