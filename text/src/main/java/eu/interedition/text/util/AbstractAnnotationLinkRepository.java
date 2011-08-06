package eu.interedition.text.util;

import eu.interedition.text.AnnotationLink;
import eu.interedition.text.AnnotationLinkRepository;
import eu.interedition.text.QName;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public abstract class AbstractAnnotationLinkRepository implements AnnotationLinkRepository {
  public Iterable<AnnotationLink> create(QName... names) {
    return create(Arrays.asList(names));
  }

  public void set(AnnotationLink link, QName name, String value) {
    set(link, Collections.singletonMap(name, value));
  }

  public String get(AnnotationLink link, QName name) {
    return get(link).get(name);
  }

  public void delete(AnnotationLink link, QName name) {
    delete(link, Collections.singleton(name));
  }
}
