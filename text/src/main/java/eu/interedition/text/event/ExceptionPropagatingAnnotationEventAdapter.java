package eu.interedition.text.event;

import com.google.common.base.Throwables;
import eu.interedition.text.Annotation;
import eu.interedition.text.QName;
import eu.interedition.text.Range;

import java.util.Map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class ExceptionPropagatingAnnotationEventAdapter implements AnnotationEventListener {
  public void start() {
    try {
      doStart();
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  public void start(long offset, Map<Annotation, Map<QName, String>> annotations) {
    try {
      doStart(offset, annotations);
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  public void empty(long offset, Map<Annotation, Map<QName, String>> annotations) {
    try {
      doEmpty(offset, annotations);
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  public void end(long offset, Map<Annotation, Map<QName, String>> annotations) {
    try {
      doEnd(offset, annotations);
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  public void text(Range r, String text) {
    try {
      doText(r, text);
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  public void end() {
    try {
      doEnd();
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  protected void doStart() throws Exception {
  }

  protected void doStart(long offset, Map<Annotation, Map<QName, String>> annotations) throws Exception {
  }

  protected void doEmpty(long offset, Map<Annotation, Map<QName, String>> annotations) throws Exception {
  }

  protected void doEnd(long offset, Map<Annotation, Map<QName, String>> annotations) throws Exception {
  }

  protected void doText(Range r, String text) throws Exception {
  }

  protected void doEnd() throws Exception {
  }
}
