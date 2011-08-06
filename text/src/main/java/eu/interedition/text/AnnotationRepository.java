package eu.interedition.text;

import eu.interedition.text.query.Criterion;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

public interface AnnotationRepository {

  SortedSet<QName> names(Text text);

  Iterable<Annotation> create(Annotation... annotations);

  Iterable<Annotation> create(Iterable<Annotation> annotations);

  Iterable<Annotation> find(Criterion criterion);

  void delete(Criterion criterion);

  Iterable<AnnotationLink> createLink(QName... names);

  Iterable<AnnotationLink> createLink(Iterable<QName> names);

  Map<AnnotationLink, Set<Annotation>> findLinks(Criterion criterion);

  void deleteLinks(Criterion criterion);

  void add(AnnotationLink to, Set<Annotation> toAdd);

  void remove(AnnotationLink from, Set<Annotation> toRemove);
}
