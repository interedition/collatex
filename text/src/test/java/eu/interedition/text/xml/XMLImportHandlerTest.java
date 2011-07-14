/**
 * Layered Markup and Annotation Language for Java (lmnl4j):
 * implementation of LMNL, a markup language supporting layered and/or
 * overlapping annotations.
 *
 * Copyright (C) 2010 the respective authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.text.xml;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import eu.interedition.text.*;
import eu.interedition.text.mem.SimpleQName;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertTrue;

/**
 * Tests the generation of LOMs from XML sources.
 *
 * @author <a href="http://gregor.middell.net/" title="Homepage of Gregor Middell">Gregor Middell</a>
 */
public class XMLImportHandlerTest extends AbstractXMLTest {

  @Autowired
  private TextRepository textRepository;

  @Autowired
  private AnnotationRepository annotationRepository;

  @Test
  public void showTextContents() throws IOException {
    //final String resource = "ignt-0101.xml";
    final String resource = "george-algabal-tei.xml";
    //final String resource = "homer-iliad-tei.xml";
    final Text source = source(resource);
    final Text document = document(resource);

    final AnnotationLink link = annotationRepository.createLink(new SimpleQName(TEST_NS, "link"));
    annotationRepository.add(link, Sets.<Annotation>newHashSet(annotationRepository.find(document)));

    final Map<AnnotationLink,Set<Annotation>> links = annotationRepository.findLinks(Collections.singleton(document), null, null, null);
    Assert.assertEquals(1, links.keySet().size());
    Assert.assertEquals(link, links.keySet().iterator().next());
    Assert.assertEquals(Iterables.size(annotationRepository.find(document)), Iterables.size(links.values().iterator().next()));

    LOG.info(Joiner.on('\n').join(annotationRepository.names(source)));

    final int textLength = textRepository.length(document);
    assertTrue(textLength > 0);

    if (LOG.isDebugEnabled()) {
      textRepository.read(document, new TextRepository.TextReader() {

        public void read(Reader content, int contentLength) throws IOException {
          LOG.debug(CharStreams.toString(content));
        }
      });
    }
  }
}
