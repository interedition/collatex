package eu.interedition.text.xml;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.NullOutputStream;
import eu.interedition.text.AbstractTestResourceTest;
import eu.interedition.text.QName;
import eu.interedition.text.Text;
import eu.interedition.text.mem.SimpleQName;
import eu.interedition.text.query.Criteria;
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
import java.io.PrintStream;
import java.net.URI;
import java.util.Map;
import java.util.Set;

import static eu.interedition.text.query.Criteria.*;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class XMLSerializerTest extends AbstractTestResourceTest {

  @Autowired
  private XMLSerializer xmlSerializer;

  @Test
  public void simpleSerialize() throws XMLStreamException, IOException, TransformerException, ParserConfigurationException {
    final Text testText = text("wp-orpheus1-clix.xml");

    annotationRepository.delete(and(Criteria.text(testText), rangeLength(0)));

    final SAXTransformerFactory transformerFactory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
    final TransformerHandler transformerHandler = transformerFactory.newTransformerHandler();
    transformerHandler.setResult(new StreamResult(LOG.isDebugEnabled() ? System.out : NULL_STREAM));

    xmlSerializer.serialize(transformerHandler, testText, new XMLSerializerConfiguration() {
      public QName getRootName() {
        return null;
      }

      public Map<String, URI> getNamespaceMappings() {
        return Maps.newHashMap();
      }

      public Set<QName> getHierarchy() {
        return Sets.<QName>newHashSet(
                new SimpleQName((URI) null, "phr"),
                new SimpleQName((URI) null, "s")
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

  private static final PrintStream NULL_STREAM = new PrintStream(new NullOutputStream());
}
