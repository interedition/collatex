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
import eu.interedition.text.Name;
import eu.interedition.text.Text;
import eu.interedition.text.TextTarget;
import eu.interedition.text.util.OverlapAnalyzer;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class OverlapAnalyzerTest extends AbstractTestResourceTest {

  @Test
  public void analyzeNonOverlap() throws IOException {
    final OverlapAnalyzer analyzer = analyze(text());
    Assert.assertEquals(0, analyzer.getOverlapping().size());
    Assert.assertEquals(0, analyzer.getSelfOverlapping().size());
  }

  @Test
  public void analyzeSelfOverlap() throws IOException {
    final Name overlap = new Name(TEST_NS, "overlap");
    Annotation.create(sessionFactory.getCurrentSession(),
            new Annotation(overlap, new TextTarget(text, 0, TEST_TEXT.length() - 1), null),
            new Annotation(overlap, new TextTarget(text, 1, TEST_TEXT.length()), null)
    );
    final OverlapAnalyzer analyzer = analyze(text);
    Assert.assertEquals(0, analyzer.getOverlapping().size());
    Assert.assertEquals(1, analyzer.getSelfOverlapping().size());
    Assert.assertEquals(overlap, Iterables.getOnlyElement(analyzer.getSelfOverlapping()));
  }

  protected OverlapAnalyzer analyze(Text text) throws IOException {
    return new OverlapAnalyzer().analyze(sessionFactory.getCurrentSession(), text);
  }
}
