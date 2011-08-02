package eu.interedition.text;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

public interface AnnotationRepository {

  SortedSet<QName> names(Text text);

  Annotation create(Text text, QName name, Range range);

  Iterable<Annotation> find(AnnotationPredicate predicate);

  void delete(AnnotationPredicate predicate);

  AnnotationLink createLink(QName name);

  Map<AnnotationLink, Set<Annotation>> findLinks(Set<Text> texts, Set<QName> setNames, Set<QName> names, Map<Text, Set<Range>> ranges);

  void delete(AnnotationLink annotationLink);

  void add(AnnotationLink to, Set<Annotation> toAdd);

  void remove(AnnotationLink from, Set<Annotation> toRemove);
}
