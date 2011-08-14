package eu.interedition.text.event;

import com.google.common.collect.Iterables;
import eu.interedition.text.AbstractTestResourceTest;
import eu.interedition.text.Annotation;
import eu.interedition.text.QName;
import eu.interedition.text.Range;
import eu.interedition.text.mem.SimpleQName;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Map;

import static eu.interedition.text.TextConstants.TEI_NS;
import static eu.interedition.text.query.Criteria.annotationName;
import static eu.interedition.text.query.Criteria.or;

public class AnnotationEventSourceTest extends AbstractTestResourceTest {

  @Autowired
  private AnnotationEventSource source;

  @Test
  public void generateEvents() throws IOException {
    source.listen(DEBUG_LISTENER, text("george-algabal-tei.xml"),
            or(
                    annotationName(new SimpleQName(TEI_NS, "div")),
                    annotationName(new SimpleQName(TEI_NS, "lg")),
                    annotationName(new SimpleQName(TEI_NS, "l")),
                    annotationName(new SimpleQName(TEI_NS, "p"))
            ));
  }

  private final AnnotationEventListener DEBUG_LISTENER = new AnnotationEventListener() {

    public void start() {
    }

    public void start(long offset, Map<Annotation, Map<QName, String>> annotations) {
      LOG.debug("START: [" + offset + "] " + Iterables.toString(annotations.keySet()));
    }

    public void empty(long offset, Map<Annotation, Map<QName, String>> annotations) {
      LOG.debug("EMPTY: [" + offset + "] " + Iterables.toString(annotations.keySet()));
    }

    public void end(long offset, Map<Annotation, Map<QName, String>> annotations) {
      LOG.debug("END: [" + offset + "] " + Iterables.toString(annotations.keySet()));
    }

    public void text(Range r, String text) {
      LOG.debug("TEXT: " + r + " == \"" + escapeNewlines(text) + "\"");
    }

    public void end() {
    }
  };
}
