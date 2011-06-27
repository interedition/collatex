package eu.interedition.text.event;

import eu.interedition.text.Annotation;
import eu.interedition.text.Range;

import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TextEventAdapter implements TextEventListener {
  public void start() {
  }

  public void start(int offset, Set<Annotation> annotations) {
  }

  public void empty(int offset, Set<Annotation> annotations) {
  }

  public void end(int offset, Set<Annotation> annotations) {
  }

  public void text(Range r, char[] text) {
  }

  public void end() {
  }
}
