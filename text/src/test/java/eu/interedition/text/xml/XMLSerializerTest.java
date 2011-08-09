package eu.interedition.text.xml;

import com.google.common.collect.Maps;
import eu.interedition.text.AbstractTestResourceTest;
import eu.interedition.text.QName;
import eu.interedition.text.mem.SimpleQName;
import eu.interedition.text.query.Criterion;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.Map;
import java.util.Set;

import static eu.interedition.text.query.Criteria.annotationName;
import static eu.interedition.text.query.Criteria.or;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class XMLSerializerTest extends AbstractTestResourceTest {

  @Autowired
  private XMLSerializer xmlSerializer;

  @Test
  public void simpleSerialize() throws XMLStreamException, IOException, TransformerException, ParserConfigurationException {
    final SAXTransformerFactory transformerFactory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
    final TransformerHandler transformerHandler = transformerFactory.newTransformerHandler();
    if (LOG.isDebugEnabled()) {
      transformerHandler.setResult(new StreamResult(System.out));
    } else {
      transformerHandler.setResult(new StreamResult(new Writer() {
        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void close() throws IOException {
        }
      }));
    }

    xmlSerializer.serialize(transformerHandler, text("george-algabal-tei.xml"), new XMLSerializerConfiguration() {
      public QName getRootName() {
        return new SimpleQName(TEI_NS, "text");
      }

      public Map<String, URI> getNamespaceMappings() {
        Map<String, URI> mappings = Maps.newHashMap();
        mappings.put("", TEI_NS);
        return mappings;
      }

      public Set<QName> getHierarchy() {
        return null;
      }

      public Criterion getQuery() {
        return or(
                annotationName(new SimpleQName(TEI_NS, "head")),
                annotationName(new SimpleQName(TEI_NS, "lg")),
                annotationName(new SimpleQName(TEI_NS, "l"))
        );
      }
    });

    if (LOG.isDebugEnabled()) {
      System.out.println();
    }
  }
}
