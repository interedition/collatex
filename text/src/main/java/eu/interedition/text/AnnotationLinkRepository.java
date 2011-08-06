package eu.interedition.text;

import eu.interedition.text.query.Criterion;

import java.util.Map;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface AnnotationLinkRepository {
  Iterable<AnnotationLink> create(QName... names);

  Iterable<AnnotationLink> create(Iterable<QName> names);

  Map<AnnotationLink, Set<Annotation>> find(Criterion criterion);

  void delete(Criterion criterion);

  void add(AnnotationLink to, Set<Annotation> toAdd);

  void remove(AnnotationLink from, Set<Annotation> toRemove);

  void set(AnnotationLink link, Map<QName, String> data);

  void set(AnnotationLink link, QName name, String value);

  Map<QName, String> get(AnnotationLink link);

  String get(AnnotationLink link, QName name);

  void delete(AnnotationLink link, Set<QName> names);

  void delete(AnnotationLink link, QName name);
}
