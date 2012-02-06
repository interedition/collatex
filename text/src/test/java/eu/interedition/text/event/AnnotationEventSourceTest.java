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
import eu.interedition.text.Range;
import eu.interedition.text.TextListener;
import eu.interedition.text.mem.SimpleName;
import org.junit.Test;

import java.io.IOException;

import static eu.interedition.text.TextConstants.TEI_NS;
import static eu.interedition.text.query.Criteria.annotationName;
import static eu.interedition.text.query.Criteria.or;

public class AnnotationEventSourceTest extends AbstractTestResourceTest {

  @Test
  public void generateEvents() throws IOException {
    textRepository.read(text("george-algabal-tei.xml"), or(
            annotationName(new SimpleName(TEI_NS, "div")),
            annotationName(new SimpleName(TEI_NS, "lg")),
            annotationName(new SimpleName(TEI_NS, "l")),
            annotationName(new SimpleName(TEI_NS, "p"))
    ), DEBUG_LISTENER
    );
  }

  public static final TextListener DEBUG_LISTENER = new TextListener() {

    public void start(long contentLength) {
      LOG.debug("START TEXT: (" + contentLength + " character(s))");
    }

    public void start(long offset, Iterable<Annotation> annotations) {
      LOG.debug("START: [" + offset + "] " + Iterables.toString(annotations));
    }

    public void empty(long offset, Iterable<Annotation> annotations) {
      LOG.debug("EMPTY: [" + offset + "] " + Iterables.toString(annotations));
    }

    public void end(long offset, Iterable<Annotation> annotations) {
      LOG.debug("END: [" + offset + "] " + Iterables.toString(annotations));
    }

    public void text(Range r, String text) {
      LOG.debug("TEXT: " + r + " == \"" + escapeNewlines(text) + "\"");
    }

    public void end() {
    }
  };
}
