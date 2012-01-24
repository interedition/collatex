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
import com.google.common.collect.Maps;
import eu.interedition.text.Annotation;
import eu.interedition.text.AnnotationRepository;
import eu.interedition.text.Name;
import eu.interedition.text.xml.XMLParserState;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public abstract class AbstractAnnotationXMLParserModule extends XMLParserModuleAdapter {
  protected final AnnotationRepository annotationRepository;
  protected final int batchSize;

  private List<Annotation> annotationBatch;

  protected AbstractAnnotationXMLParserModule(AnnotationRepository annotationRepository, int batchSize) {
    this.annotationRepository = annotationRepository;
    this.batchSize = batchSize;
  }

  @Override
  public void start(XMLParserState state) {
    super.start(state);
    annotationBatch = Lists.newArrayList();
  }

  @Override
  public void end(XMLParserState state) {
    if (!annotationBatch.isEmpty()) {
      emit();
    }

    annotationBatch = null;
    super.end(state);
  }

  protected void add(Annotation annotation) {
    annotationBatch.add(annotation);

    if ((annotationBatch.size() % batchSize) == 0) {
      emit();
    }
  }

  protected void emit() {
    annotationRepository.create(annotationBatch);
    annotationBatch.clear();
  }

}
