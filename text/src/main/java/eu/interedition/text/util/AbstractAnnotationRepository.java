package eu.interedition.text.util;

import eu.interedition.text.Annotation;
import eu.interedition.text.AnnotationLink;
import eu.interedition.text.AnnotationRepository;
import eu.interedition.text.QName;

import java.util.Arrays;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public abstract class AbstractAnnotationRepository implements AnnotationRepository {
  public Iterable<Annotation> create(Annotation... annotations) {
    return create(Arrays.asList(annotations));
  }

  public Iterable<AnnotationLink> createLink(QName... names) {
    return createLink(Arrays.asList(names));
  }
}
