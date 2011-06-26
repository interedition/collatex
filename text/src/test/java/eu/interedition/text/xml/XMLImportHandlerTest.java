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
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import eu.interedition.text.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import static eu.interedition.text.xml.XMLParser.OFFSET_DELTA_NAME;
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

    final int textLength = textRepository.length(document);
    assertTrue(textLength > 0);

    if (LOG.isDebugEnabled()) {
      final SortedMap<String, Annotation> annotations = Maps.newTreeMap();
      for (Annotation annotation : annotationRepository.find(document)) {
        final Object data = annotation.getData();
        if (data == null || !Map.class.isAssignableFrom(data.getClass())) {
          LOG.debug(annotation + " has no attributes");
          continue;
        }
        @SuppressWarnings("unchecked")
        final Map<QName, String> attrs = (Map<QName, String>) data;
        final String nodePath = attrs.get(XMLParser.NODE_PATH_NAME);
        if (nodePath == null) {
          LOG.debug(annotation + " has no XML node path");
          continue;
        }
        if (annotations.containsKey(nodePath)) {
          LOG.debug(nodePath + " already assigned to " + annotations.get(nodePath));
        }
        annotations.put(nodePath, annotation);
      }
      for (Map.Entry<String, Annotation> annotation : annotations.entrySet()) {
        LOG.debug(annotation.getKey() + " ==> " + annotation.getValue());
      }

      if (LOG.isDebugEnabled()) {
        textRepository.read(document, new TextContentReader() {

          public void read(Reader content, int contentLength) throws IOException {
            LOG.debug(CharStreams.toString(content));
          }
        });
      }

      final List<Range> textRanges = Lists.newArrayList();
      final List<Range> sourceRanges = Lists.newArrayList();

      for (Annotation offset : annotationRepository.find(document, OFFSET_DELTA_NAME)) {
        textRanges.add(offset.getRange());
        sourceRanges.add((Range) offset.getData());
      }

      final SortedMap<Range, String> texts = textRepository.bulkRead(document, Sets.newTreeSet(textRanges));
      final SortedMap<Range, String> sources = textRepository.bulkRead(source, Sets.newTreeSet(sourceRanges));

      final Iterator<Range> sourceRangesIt = sourceRanges.iterator();
      for (Range textRange : textRanges) {
        if (!sourceRangesIt.hasNext()) {
          break;
        }
        final Range sourceRange = sourceRangesIt.next();
        //LOG.debug(textRange + " ==> " + sourceRange);
        LOG.debug(texts.get(textRange) + " ==> " + sources.get(sourceRange));
      }
    }

  }
}
