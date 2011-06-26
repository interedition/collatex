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

import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;

import javax.xml.transform.stream.StreamSource;

import org.junit.After;
import org.junit.Before;
import eu.interedition.text.rdbms.RelationalAnnotationFactory;
import eu.interedition.text.xml.XMLParser;
import eu.interedition.text.xml.SimpleXMLParserConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Base class for tests working with documents generated from XML test resources.
 * 
 * @author <a href="http://gregor.middell.net/" title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
@Transactional
public abstract class AbstractXMLTest extends AbstractTest {
	protected static final URI TEI_NS = URI.create("http://www.tei-c.org/ns/1.0");

	/**
	 * Names of available XML test resources.
	 */
	protected static final SortedSet<String> RESOURCES = Sets.newTreeSet(Lists.newArrayList(//
			"ignt-0101.xml", "archimedes-palimpsest-tei.xml", "george-algabal-tei.xml", "homer-iliad-tei.xml"));


    private Map<String, Text> sources = Maps.newHashMap();
	private Map<String, Text> documents = Maps.newHashMap();

	private SimpleXMLParserConfiguration parserConfiguration = new SimpleXMLParserConfiguration();

	@Autowired
	private RelationalAnnotationFactory annotationFactory;

	@Autowired
	private XMLParser xmlParser;

	@Before
	public void configureXMLParser() {
		parserConfiguration.addLineElement(new QNameImpl(TEI_NS, "lg"));
		parserConfiguration.addLineElement(new QNameImpl(TEI_NS, "l"));
		parserConfiguration.addLineElement(new QNameImpl(TEI_NS, "speaker"));
		parserConfiguration.addLineElement(new QNameImpl(TEI_NS, "stage"));
		parserConfiguration.addLineElement(new QNameImpl(TEI_NS, "head"));
		parserConfiguration.addLineElement(new QNameImpl(TEI_NS, "p"));

		parserConfiguration.addContainerElement(new QNameImpl(TEI_NS, "text"));
		parserConfiguration.addContainerElement(new QNameImpl(TEI_NS, "div"));
		parserConfiguration.addContainerElement(new QNameImpl(TEI_NS, "lg"));
		parserConfiguration.addContainerElement(new QNameImpl(TEI_NS, "subst"));
		parserConfiguration.addContainerElement(new QNameImpl(TEI_NS, "choice"));

		parserConfiguration.exclude(new QNameImpl(TEI_NS, "teiHeader"));
		parserConfiguration.exclude(new QNameImpl(TEI_NS, "front"));
		parserConfiguration.exclude(new QNameImpl(TEI_NS, "fw"));

		parserConfiguration.addNotableElement(new QNameImpl(TEI_NS, "lb"));
	}

	@After
	public void removeDocuments() {
		for (Iterator<Text> documentIt = documents.values().iterator(); documentIt.hasNext();) {
			annotationFactory.delete(documentIt.next());
			documentIt.remove();
		}
	}

	/**
	 * Returns a test document generated from the resource with the given name.
	 * 
	 * <p/>
	 * 
	 * The generated test document is cached for later reuse.
	 * 
	 *
     * @param resource
     *                the name of the resource
     * @return the corresponding test document
	 * @see #RESOURCES
	 */
	protected synchronized Text document(String resource) {
        load(resource);
		return documents.get(resource);
	}

    protected synchronized Text source(String resource) {
        load(resource);
        return sources.get(resource);
    }

    protected synchronized void load(String resource) {
        try {
            if (RESOURCES.contains(resource) && !documents.containsKey(resource)) {

                Text xml = annotationFactory.newText();
                final URI uri = AbstractXMLTest.class.getResource("/" + resource).toURI();

                xmlParser.load(xml, new StreamSource(uri.toASCIIString()));
                sources.put(resource, xml);

                final Text text = annotationFactory.newText();
                xmlParser.parse(xml, text, parserConfiguration);

                documents.put(resource, text);
            }
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

	/**
	 * Returns a default test document.
	 * 
	 * @return the document generated from the first available test resource
	 */
	protected Text document() {
		return document(RESOURCES.first());
	}
}
