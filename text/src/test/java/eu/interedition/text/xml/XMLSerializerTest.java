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
import com.google.common.collect.Maps;
import com.google.common.io.NullOutputStream;
import eu.interedition.text.AbstractTestResourceTest;
import eu.interedition.text.Name;
import eu.interedition.text.Text;
import eu.interedition.text.mem.SimpleName;
import eu.interedition.text.query.Criteria;
import eu.interedition.text.query.Criterion;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.ContentHandler;

import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.PrintStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.interedition.text.TextConstants.TEI_NS;
import static eu.interedition.text.query.Criteria.*;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class XMLSerializerTest extends AbstractTestResourceTest {

  private static final PrintStream NULL_STREAM = new PrintStream(new NullOutputStream());

  @Autowired
  private XMLSerializer xmlSerializer;

  private SAXTransformerFactory transformerFactory;

  @Before
  public void initTransformerFactory() {
    transformerFactory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
  }

  @Test
  public void clixSerialize() throws Exception {
    final Text testText = text("wp-orpheus1-clix.xml");

    annotationRepository.delete(and(Criteria.text(testText), rangeLength(0)));

    xmlSerializer.serialize(createOutputHandler(), testText, new XMLSerializerConfiguration() {
      public Name getRootName() {
        return null;
      }

      public Map<String, URI> getNamespaceMappings() {
        return Maps.newHashMap();
      }

      public List<Name> getHierarchy() {
        return Lists.<Name>newArrayList(
                new SimpleName((URI) null, "phr"),
                new SimpleName((URI) null, "s")
        );
      }

      public Criterion getQuery() {
        return any();
      }
    });

    if (LOG.isDebugEnabled()) {
      System.out.println();
    }
  }

  @Test
  public void teiConversion() throws Exception {
    final Text testText = text("george-algabal-tei.xml");
    annotationRepository.delete(and(Criteria.text(testText), rangeLength(0)));
    xmlSerializer.serialize(createOutputHandler(), testText, new XMLSerializerConfiguration() {
      public Name getRootName() {
        return new SimpleName(TEI_NS, "text");
      }

      public Map<String, URI> getNamespaceMappings() {
        final HashMap<String,URI> nsMap = Maps.newHashMap();
        nsMap.put("", TEI_NS);
        return nsMap;
      }

      public List<Name> getHierarchy() {
        return Lists.<Name>newArrayList(
                new SimpleName(TEI_NS, "page"),
                new SimpleName(TEI_NS, "line")
        );
      }

      public Criterion getQuery() {
        return or(
                annotationName(new SimpleName(TEI_NS, "page")),
                annotationName(new SimpleName(TEI_NS, "line"))
        );
      }
    });

    if (LOG.isDebugEnabled()) {
      System.out.println();
    }
  }

  protected ContentHandler createOutputHandler() throws Exception {
    final TransformerHandler transformerHandler = transformerFactory.newTransformerHandler();
    transformerHandler.setResult(new StreamResult(LOG.isDebugEnabled() ? System.out : NULL_STREAM));
    return transformerHandler;
  }
}
