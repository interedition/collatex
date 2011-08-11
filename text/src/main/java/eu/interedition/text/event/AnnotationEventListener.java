package eu.interedition.text.event;

import eu.interedition.text.Annotation;
import eu.interedition.text.QName;
import eu.interedition.text.Range;

import java.util.Map;


public interface AnnotationEventListener {

  void start();

  void start(long offset, Map<Annotation, Map<QName, String>> annotations);

  void empty(long offset, Map<Annotation, Map<QName, String>> annotations);

  void end(long offset, Map<Annotation, Map<QName, String>> annotations);

  void text(Range r, String text);

  void end();
}
