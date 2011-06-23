package eu.interedition.collatex.persistence;

import com.google.common.base.Strings;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import eu.interedition.collatex2.implementation.CollateXEngine;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.junit.Test;
import org.lmnl.QNameImpl;
import org.lmnl.Text;
import org.lmnl.TextContentReader;
import org.lmnl.TextRepository;
import org.lmnl.rdbms.RelationalAnnotationFactory;
import org.lmnl.xml.SimpleXMLParserConfiguration;
import org.lmnl.xml.XMLParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Transactional
public class SimpleCollationTest extends AbstractTest {
  private static final Logger LOG = LoggerFactory.getLogger(SimpleCollationTest.class);

  private static final String TEI_NS = "http://www.tei-c.org/ns/1.0";

  @Autowired
  private RelationalAnnotationFactory annotationFactory;

  @Autowired
  private XMLParser parser;

  @Autowired
  private TextRepository textRepository;

  @Autowired
  private SessionFactory sessionFactory;

  private CollateXEngine engine = new CollateXEngine();

  @Test
  public void collate() throws IOException, TransformerException, XMLStreamException {
    final Session session = sessionFactory.getCurrentSession();

    final Collation collation = new Collation();
    session.save(collation);

    final SimpleXMLParserConfiguration parserConfiguration = new SimpleXMLParserConfiguration();

    parserConfiguration.exclude(new QNameImpl(TEI_NS, "teiHeader"));

    parserConfiguration.addLineElement(new QNameImpl(TEI_NS, "lb"));

    for (String resource : new String[]{"0101.xml", "0105.xml", "0109.xml"}) {
      final Witness witness = new Witness();
      witness.setCollation(collation);

      final URL r = getClass().getResource("/samples/igntp/" + resource);
      LOG.info("Importing " + r);

      InputStream stream = null;
      try {
        final Text source = annotationFactory.newText();
        parser.load(source, new StreamSource(stream = r.openStream()));
        witness.setSource(source);

        final Text text = annotationFactory.newText();
        parser.parse(source, text, parserConfiguration);
        witness.setText(text);
      } finally {
        Closeables.closeQuietly(stream);
      }

      session.save(witness);

      System.out.println();
      System.out.println(Strings.repeat("=", 80));
      textRepository.read(witness.getText(), new TextContentReader() {
        @Override
        public void read(Reader content, int contentLength) throws IOException {
          CharStreams.copy(content, System.out);
        }
      });
      System.out.println();
      System.out.println(Strings.repeat("=", 80));
    }
  }
}
