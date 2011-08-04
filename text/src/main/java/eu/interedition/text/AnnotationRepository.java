package eu.interedition.text;

import eu.interedition.text.predicate.AnnotationPredicate;
import eu.interedition.text.predicate.Predicate;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

public interface AnnotationRepository {

  SortedSet<QName> names(Text text);

  Iterable<Annotation> create(Annotation... annotations);

  Iterable<Annotation> create(Iterable<Annotation> annotations);

  Iterable<Annotation> find(Iterable<AnnotationPredicate> predicates);

  Iterable<Annotation> find(AnnotationPredicate... predicates);

  void delete(Iterable<AnnotationPredicate> predicates);

  void delete(AnnotationPredicate... predicates);

  Iterable<AnnotationLink> createLink(QName... names);

  Iterable<AnnotationLink> createLink(Iterable<QName> names);

  Map<AnnotationLink, Set<Annotation>> findLinks(Iterable<Predicate> predicates);

  Map<AnnotationLink, Set<Annotation>> findLinks(Predicate... predicates);

  void deleteLinks(Iterable<Predicate> predicates);

  void deleteLinks(Predicate... predicates);

  void add(AnnotationLink to, Set<Annotation> toAdd);

  void remove(AnnotationLink from, Set<Annotation> toRemove);
}
