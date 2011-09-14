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
package eu.interedition.text.event;

import com.google.common.collect.Iterables;
import eu.interedition.text.AbstractTestResourceTest;
import eu.interedition.text.Annotation;
import eu.interedition.text.QName;
import eu.interedition.text.Range;
import eu.interedition.text.mem.SimpleQName;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static eu.interedition.text.TextConstants.TEI_NS;
import static eu.interedition.text.query.Criteria.annotationName;
import static eu.interedition.text.query.Criteria.or;

public class AnnotationEventSourceTest extends AbstractTestResourceTest {

  @Autowired
  private AnnotationEventSource source;

  @Test
  public void generateEvents() throws IOException {
    source.listen(DEBUG_LISTENER, text("george-algabal-tei.xml"),
            or(
                    annotationName(new SimpleQName(TEI_NS, "div")),
                    annotationName(new SimpleQName(TEI_NS, "lg")),
                    annotationName(new SimpleQName(TEI_NS, "l")),
                    annotationName(new SimpleQName(TEI_NS, "p"))
            ), Collections.<QName>emptySet());
  }

  private final AnnotationEventListener DEBUG_LISTENER = new AnnotationEventListener() {

    public void start() {
    }

    public void start(long offset, Map<Annotation, Map<QName, String>> annotations) {
      LOG.debug("START: [" + offset + "] " + Iterables.toString(annotations.keySet()));
    }

    public void empty(long offset, Map<Annotation, Map<QName, String>> annotations) {
      LOG.debug("EMPTY: [" + offset + "] " + Iterables.toString(annotations.keySet()));
    }

    public void end(long offset, Map<Annotation, Map<QName, String>> annotations) {
      LOG.debug("END: [" + offset + "] " + Iterables.toString(annotations.keySet()));
    }

    public void text(Range r, String text) {
      LOG.debug("TEXT: " + r + " == \"" + escapeNewlines(text) + "\"");
    }

    public void end() {
    }
  };
}
