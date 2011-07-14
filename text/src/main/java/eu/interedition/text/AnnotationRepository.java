package eu.interedition.text;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

public interface AnnotationRepository {

  Annotation create(Text text, QName name, Range range);

  AnnotationLink createLink(QName name);

  void delete(Annotation annotation);

  void delete(AnnotationLink annotationLink);

  void add(AnnotationLink to, Set<Annotation> toAdd);

  void remove(AnnotationLink from, Set<Annotation> toRemove);

  SortedSet<QName> names(Text text);

  Iterable<Annotation> find(Text text, Set<QName> names, Set<Range> ranges);

  Iterable<Annotation> find(Text text, Set<QName> names);

  Iterable<Annotation> find(Text text, QName name);

  Iterable<Annotation> find(Text text);

  Map<AnnotationLink, Set<Annotation>> findLinks(Set<Text> texts, Set<QName> setNames, Set<QName> names, Map<Text, Set<Range>> ranges);
}
