package eu.interedition.text;

import com.google.common.collect.Multimap;
import eu.interedition.text.query.Criterion;

import java.util.Map;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface AnnotationLinkRepository {
  Map<AnnotationLink, Set<Annotation>> create(Multimap<QName, Set<Annotation>> links);

  Map<AnnotationLink, Set<Annotation>> find(Criterion criterion);

  void delete(Criterion criterion);

  Map<AnnotationLink, Map<QName, String>> get(Iterable<AnnotationLink> links, Set<QName> names);

  void set(Map<AnnotationLink, Map<QName, String>> data);

  void unset(Map<AnnotationLink, Iterable<QName>> data);
}
