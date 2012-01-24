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
import eu.interedition.text.*;
import eu.interedition.text.query.Criteria;
import eu.interedition.text.transform.AnnotationTransformers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;

import java.io.IOException;
import java.io.StringReader;

import static com.google.common.collect.Iterables.size;
import static eu.interedition.text.query.Criteria.and;
import static eu.interedition.text.query.Criteria.rangeFitsWithin;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class AnnotationTest extends AbstractTestResourceTest {

  @Autowired
  private AnnotationRepository annotationRepository;

  @Test
  public void deleteAll() {
    final Text existing = text();
    try {
      annotationRepository.delete(and(Criteria.text(existing), rangeFitsWithin(new Range(0, existing.getLength()))));
      final Iterable<Annotation> remaining = annotationRepository.find(Criteria.text(existing));
      assertTrue(Integer.toString(size(remaining)) + " in " + existing, Iterables.isEmpty(remaining));
    } finally {
      unload();
    }
  }

  @Test
  public void transform() throws IOException {
    final Text existing = text("george-algabal-tei.xml");
    try {
      final int numAnnotations = size(annotationRepository.find(Criteria.text(existing)));

      final Text newText = textRepository.create(new StringReader("Hello Hello!"));

      final StopWatch sw = new StopWatch("transform");
      sw.start("shift");
      annotationRepository.transform(Criteria.text(existing), newText, AnnotationTransformers.shift(10));
      sw.stop();

      assertEquals(numAnnotations, size(annotationRepository.find(Criteria.text(newText))));

      sw.start("print");
      if (LOG.isDebugEnabled()) {
        annotationRepository.scroll(Criteria.text(newText), null, new AnnotationConsumer() {
          @Override
          public void consume(Annotation annotation) {
            LOG.debug("{}: {}", annotation, Iterables.toString(annotation.getData().entrySet()));
          }
        });
      }
      sw.stop();

      LOG.debug(sw.prettyPrint());

    } finally {
      unload();
    }
  }
}
