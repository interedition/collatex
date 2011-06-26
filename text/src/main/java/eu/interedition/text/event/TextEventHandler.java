package eu.interedition.text.event;

import eu.interedition.text.Annotation;
import eu.interedition.text.Range;

import java.util.Set;


public interface TextEventHandler {

  void start();

  void start(int offset, Set<Annotation> annotations);

  void empty(int offset, Set<Annotation> annotations);

  void end(int offset, Set<Annotation> annotations);

  void text(Range r, char[] text);

  void end();
}
