package eu.interedition.collatex.persistence;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Closeables;
import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.text.*;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.junit.Test;
import eu.interedition.text.rdbms.RelationalAnnotationFactory;
import eu.interedition.text.xml.SimpleXMLParserConfiguration;
import eu.interedition.text.xml.XMLParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.SortedMap;

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
  private AnnotationRepository annotationRepository;

  @Autowired
  private SessionFactory sessionFactory;

  @Autowired
  private Tokenizer tokenizer;

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

      tokenizer.tokenize(witness, new WhitespaceTokenizerSettings());
      printTokenizedWitness(witness);
    }
  }

  private void printTokenizedWitness(Witness witness) throws IOException {
    System.out.println(Strings.padStart(witness.toString(), 80, '='));

    final Text text = witness.getText();
    int read = 0;

    final SortedMap<Range, Boolean> ranges = Maps.newTreeMap();
    for (Annotation token : annotationRepository.find(text, Tokenizer.TOKEN_NAME)) {
        final Range range = token.getRange();
        if (read < range.getStart()) {
            ranges.put(new Range(read, range.getStart()), false);
        }
        ranges.put(token.getRange(), true);
        read = token.getRange().getEnd();
    }

    final int length = textRepository.length(text);
    if (read < length) {
        ranges.put(new Range(read, length), false);
    }

    final SortedMap<Range, String> texts = textRepository.bulkRead(text, Sets.newTreeSet(ranges.keySet()));
    for (Map.Entry<Range, Boolean> range : ranges.entrySet()) {
        System.out.print(range.getValue() ? "[" : "");
        System.out.print(texts.get(range.getKey()));
        System.out.print(range.getValue() ? "]" : "");
    }
    System.out.println();

  }
}
