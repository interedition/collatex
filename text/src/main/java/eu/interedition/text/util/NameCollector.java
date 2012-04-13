package eu.interedition.text.util;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.io.Closeables;
import eu.interedition.text.Annotation;
import eu.interedition.text.Name;
import eu.interedition.text.Text;
import eu.interedition.text.query.AnnotationListenerAdapter;
import eu.interedition.text.query.QueryCriteria;
import eu.interedition.text.xml.XML;
import org.hibernate.Session;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class NameCollector extends AnnotationListenerAdapter {
  private XMLInputFactory xmlInputFactory = XML.createXMLInputFactory();
  private SortedSet<Name> names;

  public SortedSet<Name> getNames() {
    return names;
  }

  public NameCollector collect(Session session, Text text) throws IOException, XMLStreamException {
    if (text.getType() == Text.Type.XML) {
      Reader textReader = null;
      XMLStreamReader xmlReader = null;
      try {
        names = Sets.newTreeSet();
        xmlReader = xmlInputFactory.createXMLStreamReader(textReader = text.read().getInput());
        while (xmlReader.hasNext()) {
          switch (xmlReader.next()) {
            case XMLStreamReader.START_ELEMENT:
              names.add(new Name(xmlReader.getName()));
          }
        }
      } finally {
        XML.closeQuietly(xmlReader);
        Closeables.close(textReader, false);
      }
    } else {
      QueryCriteria.any().listen(session, text, this);
    }
    return this;
  }

  @Override
  public void start(long contentLength) {
    names = Sets.newTreeSet();
  }

  @Override
  public void start(long offset, Iterable<Annotation> annotations) {
    Iterables.addAll(names, Iterables.transform(annotations, Annotation.NAME));
  }
}
