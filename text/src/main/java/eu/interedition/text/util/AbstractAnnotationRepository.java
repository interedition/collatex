package eu.interedition.text.util;

import eu.interedition.text.Annotation;
import eu.interedition.text.AnnotationLink;
import eu.interedition.text.AnnotationRepository;
import eu.interedition.text.predicate.AnnotationPredicate;
import eu.interedition.text.predicate.Predicate;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public abstract class AbstractAnnotationRepository implements AnnotationRepository {
  public void delete(AnnotationPredicate... predicates) {
    delete(Arrays.asList(predicates));
  }

  public void deleteLinks(Predicate... predicates) {
    deleteLinks(Arrays.asList(predicates));
  }

  public Iterable<Annotation> find(AnnotationPredicate... predicates) {
    return find(Arrays.asList(predicates));
  }

  public Map<AnnotationLink, Set<Annotation>> findLinks(Predicate... predicates) {
    return findLinks(Arrays.asList(predicates));
  }


}
