package eu.interedition.text.util;

import eu.interedition.text.*;

import java.util.Collections;
import java.util.Set;

public abstract class AbstractAnnotationRepository implements AnnotationRepository {


  public Iterable<Annotation> find(Text text, Set<QName> names) {
    return find(text, names, null);
  }

  public Iterable<Annotation> find(Text text, QName name) {
    return find(text, Collections.singleton(name), null);
  }

  public Iterable<Annotation> find(Text text) {
    return find(text, null, null);
  }

}
