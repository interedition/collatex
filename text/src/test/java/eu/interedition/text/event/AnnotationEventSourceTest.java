package eu.interedition.text.event;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import eu.interedition.text.AbstractXMLTest;
import eu.interedition.text.Annotation;
import eu.interedition.text.QName;
import eu.interedition.text.Range;
import eu.interedition.text.mem.SimpleQName;
import eu.interedition.text.util.SimpleAnnotationPredicate;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Set;

import static java.util.Collections.singleton;

public class AnnotationEventSourceTest extends AbstractXMLTest {

  @Autowired
  private AnnotationEventSource source;

  @Test
  public void generateEvents() throws IOException {
    source.listen(DEBUG_LISTENER, new SimpleAnnotationPredicate(null,
            singleton(document("george-algabal-tei.xml")),
            Sets.<QName>newHashSet(
                    new SimpleQName(TEI_NS, "div"),
                    new SimpleQName(TEI_NS, "lg"),
                    new SimpleQName(TEI_NS, "l"),
                    new SimpleQName(TEI_NS, "p")), null));
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
