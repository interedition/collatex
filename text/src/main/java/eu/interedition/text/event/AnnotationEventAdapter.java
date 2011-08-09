package eu.interedition.text.event;

import eu.interedition.text.Annotation;
import eu.interedition.text.QName;
import eu.interedition.text.Range;

import java.util.Map;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class AnnotationEventAdapter implements AnnotationEventListener {
  public void start() {
  }

  public void start(int offset, Map<Annotation, Map<QName, String>> annotations) {
  }

  public void empty(int offset, Map<Annotation, Map<QName, String>> annotations) {
  }

  public void end(int offset, Map<Annotation, Map<QName, String>> annotations) {
  }

  public void text(Range r, String text) {
  }

  public void end() {
  }
}
