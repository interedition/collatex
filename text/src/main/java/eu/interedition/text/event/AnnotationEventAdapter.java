package eu.interedition.text.event;

import eu.interedition.text.Annotation;
import eu.interedition.text.QName;
import eu.interedition.text.Range;

import java.util.Map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class AnnotationEventAdapter implements AnnotationEventListener {
  public void start() {
  }

  public void start(long offset, Map<Annotation, Map<QName, String>> annotations) {
  }

  public void empty(long offset, Map<Annotation, Map<QName, String>> annotations) {
  }

  public void end(long offset, Map<Annotation, Map<QName, String>> annotations) {
  }

  public void text(Range r, String text) {
  }

  public void end() {
  }
}
