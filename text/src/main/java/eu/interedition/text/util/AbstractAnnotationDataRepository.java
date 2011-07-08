package eu.interedition.text.util;

import com.google.common.collect.Maps;
import eu.interedition.text.Annotation;
import eu.interedition.text.AnnotationDataRepository;
import eu.interedition.text.QName;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public abstract class AbstractAnnotationDataRepository implements AnnotationDataRepository {


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
