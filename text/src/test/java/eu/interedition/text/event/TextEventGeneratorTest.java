package eu.interedition.text.event;

import com.google.common.collect.Iterables;
import eu.interedition.text.*;
import eu.interedition.text.mem.SimpleQName;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

public class TextEventGeneratorTest extends AbstractXMLTest {

  @Autowired
  private TextEventGenerator generator;

  @Test
  public void generateEvents() throws IOException {
    generator.generate(DEBUG_LISTENER, document(), Collections.<QName>singleton(new SimpleQName("http://www.tei-c.org/ns/1.0", "seg")));
  }

  private final TextEventListener DEBUG_LISTENER = new TextEventListener() {

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
