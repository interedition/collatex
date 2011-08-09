package eu.interedition.text.event;

import eu.interedition.text.Annotation;
import eu.interedition.text.QName;
import eu.interedition.text.Range;

import java.util.Map;
import java.util.Set;


public interface AnnotationEventListener {

  void start();

  void start(int offset, Map<Annotation, Map<QName, String>> annotations);

  void empty(int offset, Map<Annotation, Map<QName, String>> annotations);

  void end(int offset, Map<Annotation, Map<QName, String>> annotations);

  void text(Range r, String text);

  void end();
}
