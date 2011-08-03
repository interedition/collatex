package eu.interedition.text;

import eu.interedition.text.predicate.AnnotationPredicate;
import eu.interedition.text.predicate.Predicate;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

public interface AnnotationRepository {

  SortedSet<QName> names(Text text);

  Annotation create(Text text, QName name, Range range);

  Iterable<Annotation> find(Iterable<AnnotationPredicate> predicates);

  Iterable<Annotation> find(AnnotationPredicate... predicates);

  void delete(Iterable<AnnotationPredicate> predicates);

  void delete(AnnotationPredicate... predicates);

  AnnotationLink createLink(QName name);

  Map<AnnotationLink, Set<Annotation>> findLinks(Iterable<Predicate> predicates);

  Map<AnnotationLink, Set<Annotation>> findLinks(Predicate... predicates);

  void deleteLinks(Iterable<Predicate> predicates);

  void deleteLinks(Predicate... predicates);

  void add(AnnotationLink to, Set<Annotation> toAdd);

  void remove(AnnotationLink from, Set<Annotation> toRemove);
}
