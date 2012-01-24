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
package eu.interedition.text.xml;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import eu.interedition.text.*;
import eu.interedition.text.xml.module.XMLParserModuleAdapter;
import org.junit.After;
import org.junit.Assert;
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

    assertTrue(text.toString(), text.getLength() > 0);

    if (LOG.isDebugEnabled()) {
      textRepository.read(text, new TextConsumer() {

        public void read(Reader content, long contentLength) throws IOException {
          LOG.debug(CharStreams.toString(content));
        }
      });
    }
  }

  @Test
  public void offsetMapping() throws IOException {
    if (LOG.isDebugEnabled()) {
      final SortedMap<Range, String> sources = textRepository.bulkRead(source("george-algabal-tei.xml"), Sets.newTreeSet(sourceRanges));
      final SortedMap<Range, String> texts = textRepository.bulkRead(text("george-algabal-tei.xml"), Sets.newTreeSet(textRanges));
      final Iterator<Range> sourceRangeIt = sourceRanges.iterator();
      final Iterator<Range> textRangeIt = textRanges.iterator();
      while (sourceRangeIt.hasNext() && textRangeIt.hasNext()) {
        LOG.debug("[" + escapeNewlines(sources.get(sourceRangeIt.next())) +//
                "] <====> [" + escapeNewlines(texts.get(textRangeIt.next())) + "]");
      }
    }
  }

  @Test
  public void dtdParsing() {
    Assert.assertNotNull(text("whitman-leaves-facs-tei.xml"));
  }

  @Test
  public void bigFile() {
    Assert.assertNotNull(text("homer-iliad-tei.xml"));
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
