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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import eu.interedition.text.*;
import eu.interedition.text.xml.module.XMLParserModuleAdapter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;

import static junit.framework.Assert.assertTrue;

/**
 * Tests the generation of LOMs from XML sources.
 *
 * @author <a href="http://gregor.middell.net/" title="Homepage of Gregor Middell">Gregor Middell</a>
 */
public class XMLParserTest extends AbstractTestResourceTest {
  @Autowired
  private TextRepository textRepository;

  @Autowired
  private AnnotationRepository annotationRepository;

  private List<Range> sourceRanges;
  private List<Range> textRanges;

  @Before
  public void initRanges() {
    sourceRanges = Lists.newArrayList();
    textRanges = Lists.newArrayList();
  }

  @After
  public void clearRanges() {
    sourceRanges = null;
    textRanges = null;
  }


  @Test
  public void textContents() throws Exception {
    final Text text = text("george-algabal-tei.xml");

    assertTrue(text.length() > 0);

    if (LOG.isDebugEnabled()) {
      textRepository.read(text, new TextRepository.TextReader() {

        public void read(Reader content, int contentLength) throws IOException {
          LOG.debug(CharStreams.toString(content));
        }
      });
    }
  }

  @Test
  public void offsetMapping() throws IOException {
    if (LOG.isDebugEnabled()) {
      final SortedMap<Range, String> sources = textRepository.bulkRead(source(), Sets.newTreeSet(sourceRanges));
      final SortedMap<Range, String> texts = textRepository.bulkRead(text(), Sets.newTreeSet(textRanges));
      final Iterator<Range> sourceRangeIt = sourceRanges.iterator();
      final Iterator<Range> textRangeIt = textRanges.iterator();
      while (sourceRangeIt.hasNext() && textRangeIt.hasNext()) {
        LOG.debug("[" + escapeNewlines(sources.get(sourceRangeIt.next())) +//
                "] <====> [" + escapeNewlines(texts.get(textRangeIt.next())) + "]");
      }
    }
  }

  @Override
  protected List<XMLParserModule> parserModules() {
    final List<XMLParserModule> parserModules = super.parserModules();

    parserModules.add(new XMLParserModuleAdapter() {
      @Override
      public void offsetMapping(XMLParserState state, Range textRange, Range sourceRange) {
        if (LOG.isDebugEnabled()) {
          sourceRanges.add(sourceRange);
          textRanges.add(textRange);
        }
      }
    });

    return parserModules;
  }
}
