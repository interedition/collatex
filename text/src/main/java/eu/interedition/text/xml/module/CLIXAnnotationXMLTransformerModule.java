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

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import eu.interedition.text.Annotation;
import eu.interedition.text.TextConstants;
import eu.interedition.text.TextTarget;
import eu.interedition.text.xml.XMLEntity;
import eu.interedition.text.xml.XMLTransformer;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import java.util.Map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class CLIXAnnotationXMLTransformerModule extends AbstractAnnotationXMLTransformerModule {
  private Map<String, Annotation> annotations;

  public CLIXAnnotationXMLTransformerModule(int batchSize) {
    super(batchSize, false);
  }

  @Override
  public void start(XMLTransformer transformer) {
    super.start(transformer);
    annotations = Maps.newHashMap();
  }

  @Override
  public void end(XMLTransformer transformer) {
    annotations = null;
    super.end(transformer);
  }

  @Override
  public void start(XMLTransformer transformer, XMLEntity entity) {
    super.start(transformer, entity);

    final ObjectNode entityAttributes = entity.getAttributes();
    final JsonNode startId = entityAttributes.remove(TextConstants.CLIX_START_ATTR_NAME.toString());
    final JsonNode endId = entityAttributes.remove(TextConstants.CLIX_END_ATTR_NAME.toString());
    if (startId == null && endId == null) {
      return;
    }

    final long textOffset = transformer.getTextOffset();

    if (startId != null) {
      annotations.put(startId.toString(), new Annotation(entity.getName(), new TextTarget(transformer.getTarget(), textOffset, textOffset), entityAttributes));
    }
    if (endId != null) {
      final Annotation a = annotations.remove(endId.toString());
      if (a != null) {
        Iterables.getOnlyElement(a.getTargets()).setEnd(textOffset);
        add(transformer, a);
      }
    }
  }
}
