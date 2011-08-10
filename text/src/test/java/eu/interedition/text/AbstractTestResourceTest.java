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

package eu.interedition.text;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import eu.interedition.text.mem.SimpleQName;
import eu.interedition.text.util.SimpleXMLParserConfiguration;
import eu.interedition.text.xml.XMLParser;
import eu.interedition.text.xml.XMLParserModule;
import eu.interedition.text.xml.module.*;
import org.junit.After;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;

import javax.xml.transform.stream.StreamSource;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import static eu.interedition.text.TextConstants.TEI_NS;

/**
 * Base class for tests working with documents generated from XML test resources.
 *
 * @author <a href="http://gregor.middell.net/" title="Homepage of Gregor Middell">Gregor Middell</a>
 */
public abstract class AbstractTestResourceTest extends AbstractTextTest {
  /**
   * Names of available XML test resources.
   */
  protected static final SortedSet<String> RESOURCES = Sets.newTreeSet(Lists.newArrayList(//
          "wp-orpheus1-clix.xml", "george-algabal-tei.xml", "ignt-0101.xml", "archimedes-palimpsest-tei.xml", "homer-iliad-tei.xml"));


  private Map<String, Text> sources = Maps.newHashMap();
  private Map<String, Text> texts = Maps.newHashMap();

  @Autowired
  private XMLParser xmlParser;

  @Autowired
  protected AnnotationRepository annotationRepository;

  @After
  public void removeDocuments() {
    for (Iterator<Text> textIt = texts.values().iterator(); textIt.hasNext(); ) {
      textRepository.delete(textIt.next());
      textIt.remove();
    }
    for (Iterator<Text> sourceIt = sources.values().iterator(); sourceIt.hasNext(); ) {
      textRepository.delete(sourceIt.next());
      sourceIt.remove();
    }
  }

  protected Text text() {
    return text(RESOURCES.first());
  }

  protected Text source() {
    return source(RESOURCES.first());
  }

  /**
   * Returns a test document generated from the resource with the given name.
   * <p/>
   * <p/>
   * <p/>
   * The generated test document is cached for later reuse.
   *
   * @param resource the name of the resource
   * @return the corresponding test document
   * @see #RESOURCES
   */
  protected synchronized Text text(String resource) {
    load(resource);
    return texts.get(resource);
  }

  protected synchronized Text source(String resource) {
    load(resource);
    return sources.get(resource);
  }

  protected List<XMLParserModule> parserModules() {
    return Lists.<XMLParserModule>newArrayList();
  }

  protected SimpleXMLParserConfiguration configure(SimpleXMLParserConfiguration pc) {
    pc.addLineElement(new SimpleQName(TEI_NS, "head"));
    pc.addLineElement(new SimpleQName(TEI_NS, "stage"));
    pc.addLineElement(new SimpleQName(TEI_NS, "speaker"));
    pc.addLineElement(new SimpleQName(TEI_NS, "lg"));
    pc.addLineElement(new SimpleQName(TEI_NS, "l"));
    pc.addLineElement(new SimpleQName((URI) null, "line"));

    pc.addContainerElement(new SimpleQName(TEI_NS, "text"));
    pc.addContainerElement(new SimpleQName(TEI_NS, "div"));
    pc.addContainerElement(new SimpleQName(TEI_NS, "lg"));
    pc.addContainerElement(new SimpleQName(TEI_NS, "subst"));
    pc.addContainerElement(new SimpleQName(TEI_NS, "choice"));

    pc.exclude(new SimpleQName(TEI_NS, "teiHeader"));
    pc.exclude(new SimpleQName(TEI_NS, "front"));
    pc.exclude(new SimpleQName(TEI_NS, "fw"));

    return pc;
  }

  protected SimpleXMLParserConfiguration createXMLParserConfiguration() {
    SimpleXMLParserConfiguration pc = new SimpleXMLParserConfiguration();

    final List<XMLParserModule> parserModules = pc.getModules();
    parserModules.add(new LineElementXMLParserModule());
    parserModules.add(new NotableCharacterXMLParserModule());
    parserModules.add(new TextXMLParserModule(textRepository));
    parserModules.add(new DefaultAnnotationXMLParserModule(annotationRepository, 1000));
    parserModules.add(new CLIXAnnotationXMLParserModule(annotationRepository, 1000));
    parserModules.add(new TEIAwareAnnotationXMLParserModule(annotationRepository, 1000));
    parserModules.addAll(parserModules());

    return pc;
  }

  protected synchronized void unload(String resource) {
    sources.remove(resource);
    texts.remove(resource);
  }

  protected void unload() {
    unload(RESOURCES.first());
  }

  private synchronized void load(String resource) {
    try {
      if (RESOURCES.contains(resource) && !texts.containsKey(resource)) {
        final URI uri = AbstractTestResourceTest.class.getResource("/" + resource).toURI();
        final StopWatch stopWatch = new StopWatch(uri.toString());

        stopWatch.start("create");
        final Text xml = textRepository.create(new StreamSource(uri.toASCIIString()));
        stopWatch.stop();

        sources.put(resource, xml);
        stopWatch.start("parse");
        texts.put(resource, xmlParser.parse(xml, configure(createXMLParserConfiguration())));
        stopWatch.stop();

        if (LOG.isDebugEnabled()) {
          LOG.debug("\n" + stopWatch.prettyPrint());
        }
      }
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }


}
