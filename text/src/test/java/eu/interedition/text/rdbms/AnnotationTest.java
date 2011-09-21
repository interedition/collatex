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

import com.google.common.base.Functions;
import com.google.common.collect.Iterables;
import eu.interedition.text.*;
import eu.interedition.text.query.Criteria;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

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
  public void delete() {
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
  public void transform() throws IOException {
    final Text existing = text("george-algabal-tei.xml");
    try {
      if (LOG.isDebugEnabled()) {
        annotationRepository.scroll(Criteria.text(existing), null, new AnnotationRepository.AnnotationCallback() {
          @Override
          public void annotation(Annotation annotation, Map<QName, String> data) {
            LOG.debug("{}: {}", annotation, Iterables.toString(data.entrySet()));
          }
        });
      }

      final int numAnnotations = size(annotationRepository.find(Criteria.text(existing)));

      final Text newText = textRepository.create(new StringReader("Hello Hello!"));
      annotationRepository.transform(Criteria.text(existing), newText, Functions.<Annotation>identity());

      assertEquals(numAnnotations, size(annotationRepository.find(Criteria.text(newText))));
    } finally {
      unload();
    }
  }
}
