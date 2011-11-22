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

import com.google.common.collect.Maps;
import eu.interedition.text.AnnotationRepository;
import eu.interedition.text.Name;
import eu.interedition.text.Range;
import eu.interedition.text.TextConstants;
import eu.interedition.text.mem.SimpleAnnotation;
import eu.interedition.text.xml.XMLEntity;
import eu.interedition.text.xml.XMLParserState;

import java.util.Map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class CLIXAnnotationXMLParserModule extends AbstractAnnotationXMLParserModule {
  private Map<String, SimpleAnnotation> annotations;

  public CLIXAnnotationXMLParserModule(AnnotationRepository annotationRepository, int batchSize) {
    super(annotationRepository, batchSize);
  }

  @Override
  public void start(XMLParserState state) {
    super.start(state);
    annotations = Maps.<String, SimpleAnnotation>newHashMap();
  }

  @Override
  public void end(XMLParserState state) {
    annotations = null;
    super.end(state);
  }

  @Override
  public void start(XMLEntity entity, XMLParserState state) {
    super.start(entity, state);

    final Map<Name, String> entityAttributes = Maps.newHashMap(entity.getAttributes());
    final String startId = entityAttributes.remove(TextConstants.CLIX_START_ATTR_NAME);
    final String endId = entityAttributes.remove(TextConstants.CLIX_END_ATTR_NAME);
    if (startId == null && endId == null) {
      return;
    }

    final long textOffset = state.getTextOffset();

    if (startId != null) {
      annotations.put(startId, new SimpleAnnotation(state.getTarget(), entity.getName(), new Range(textOffset, textOffset), entityAttributes));
    }
    if (endId != null) {
      final SimpleAnnotation a = annotations.remove(endId);
      if (a != null) {
        add(new SimpleAnnotation(a.getText(), a.getName(), new Range(a.getRange().getStart(), textOffset), a.getData()));
      }
    }
  }
}
