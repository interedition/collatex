package eu.interedition.text;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

public interface AnnotationRepository {

  Annotation create(Text text, QName name, Range range);

  AnnotationSet createSet(QName name);

  void delete(Annotation annotation);

  void delete(AnnotationSet annotationSet);

  void add(AnnotationSet to, Set<Annotation> toAdd);

  void remove(AnnotationSet from, Set<Annotation> toRemove);

  SortedSet<QName> names(Text text);

  Iterable<Annotation> find(Text text, Set<QName> names, Set<Range> ranges, boolean overlapping);

  Iterable<Annotation> find(Text text, Set<QName> names, Set<Range> ranges);

  Iterable<Annotation> find(Text text, Set<QName> names);

  Iterable<Annotation> find(Text text, QName name);

  Iterable<Annotation> find(Text text);

  Map<AnnotationSet, Set<Annotation>> findSets(Set<Text> texts, Set<QName> setNames, Set<QName> names, Set<Range> ranges);
}
