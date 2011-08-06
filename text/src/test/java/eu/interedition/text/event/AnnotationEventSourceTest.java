package eu.interedition.text.event;

import com.google.common.collect.Iterables;
import eu.interedition.text.AbstractXMLTest;
import eu.interedition.text.Annotation;
import eu.interedition.text.Range;
import eu.interedition.text.mem.SimpleQName;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Set;

import static eu.interedition.text.query.Criteria.annotationName;
import static eu.interedition.text.query.Criteria.or;

public class AnnotationEventSourceTest extends AbstractXMLTest {

  @Autowired
  private AnnotationEventSource source;

  @Test
  public void generateEvents() throws IOException {
    source.listen(DEBUG_LISTENER, document("george-algabal-tei.xml"),
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

    public void start(int offset, Set<Annotation> annotations) {
      printDebugMessage("START: [" + offset + "] " + Iterables.toString(annotations));
    }

    public void empty(int offset, Set<Annotation> annotations) {
      printDebugMessage("EMPTY: [" + offset + "] " + Iterables.toString(annotations));
    }

    public void end(int offset, Set<Annotation> annotations) {
      printDebugMessage("END: [" + offset + "] " + Iterables.toString(annotations));
    }

    public void text(Range r, String text) {
      printDebugMessage("TEXT: " + r + " == \"" + text + "\"");
    }

    public void end() {
    }
  };
}
