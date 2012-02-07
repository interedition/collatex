package eu.interedition.text.event;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.io.Closeables;
import eu.interedition.text.Annotation;
import eu.interedition.text.Name;
import eu.interedition.text.Text;
import eu.interedition.text.TextRepository;
import eu.interedition.text.mem.SimpleName;
import eu.interedition.text.query.Criteria;
import eu.interedition.text.util.Annotations;
import eu.interedition.text.xml.XML;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class NameCollector extends TextAdapter {
  private XMLInputFactory xmlInputFactory = XML.createXMLInputFactory();
  private SortedSet<Name> names;

  public SortedSet<Name> getNames() {
    return names;
  }

  public NameCollector collect(TextRepository repository, Text text) throws IOException, XMLStreamException {
    if (text.getType() == Text.Type.XML) {
      Reader textReader = null;
      XMLStreamReader xmlReader = null;
      try {
        names = Sets.newTreeSet();
        xmlReader = xmlInputFactory.createXMLStreamReader(textReader = repository.read(text).getInput());
        while (xmlReader.hasNext()) {
          switch (xmlReader.next()) {
            case XMLStreamReader.START_ELEMENT:
              names.add(new SimpleName(xmlReader.getName()));
          }
        }
      } finally {
        XML.closeQuietly(xmlReader);
        Closeables.close(textReader, false);
      }
    } else {
      repository.read(text, Criteria.any(), this);
    }
    return this;
  }

  @Override
  public void start(long contentLength) {
    names = Sets.newTreeSet();
  }

  @Override
  public void start(long offset, Iterable<Annotation> annotations) {
    Iterables.addAll(names, Iterables.transform(annotations, Annotations.NAME));
  }
}
