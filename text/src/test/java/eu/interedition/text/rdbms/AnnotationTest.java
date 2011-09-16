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
package eu.interedition.text.rdbms;

import com.google.common.collect.Iterables;
import eu.interedition.text.AbstractTestResourceTest;
import eu.interedition.text.Annotation;
import eu.interedition.text.AnnotationRepository;
import eu.interedition.text.Text;
import eu.interedition.text.query.Criteria;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.StringReader;

import static com.google.common.collect.Iterables.size;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class AnnotationTest extends AbstractTestResourceTest {

  @Autowired
  private AnnotationRepository annotationRepository;

  @Test
  public void deleteAnnotations() {
    final Text existing = text();
    try {
      annotationRepository.delete(Criteria.text(existing));
      final Iterable<Annotation> remaining = annotationRepository.find(Criteria.text(existing));
      assertTrue(Integer.toString(size(remaining)), Iterables.isEmpty(remaining));
    } finally {
      unload();
    }
  }

  @Test
  public void adoptAnnotations() throws IOException {
    final Text existing = text();
    try {
      final int annotations = size(annotationRepository.find(Criteria.text(existing)));

      final Text newText = textRepository.create(new StringReader("Hello Hello!"));
      annotationRepository.shift(Criteria.text(existing), newText.getLength());
      annotationRepository.adopt(Criteria.text(existing), newText);

      assertTrue(Iterables.isEmpty(annotationRepository.find(Criteria.text(existing))));
      assertEquals(annotations, size(annotationRepository.find(Criteria.text(newText))));
    } finally {
      unload();
    }
  }
}
