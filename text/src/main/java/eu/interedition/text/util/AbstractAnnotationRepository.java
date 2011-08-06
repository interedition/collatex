package eu.interedition.text.util;

import eu.interedition.text.Annotation;
import eu.interedition.text.AnnotationRepository;
import eu.interedition.text.QName;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public abstract class AbstractAnnotationRepository implements AnnotationRepository {
  public Iterable<Annotation> create(Annotation... annotations) {
    return create(Arrays.asList(annotations));
  }

  public void set(Annotation annotation, QName name, String value) {
    set(annotation, Collections.singletonMap(name, value));
  }

  public String get(Annotation annotation, QName name) {
    return get(annotation).get(name);
  }

  public void delete(Annotation annotation, QName name) {
    delete(annotation, Collections.singleton(name));
  }
}
