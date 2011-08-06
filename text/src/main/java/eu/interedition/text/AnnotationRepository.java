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

  void delete(Iterable<Annotation> annotations);

  void delete(Annotation... annotations);

  void delete(Criterion criterion);

  Map<Annotation, Map<QName, String>> get(Iterable<Annotation> links, Set<QName> names);

  void set(Map<Annotation, Map<QName, String>> data);

  void unset(Map<Annotation, Iterable<QName>> data);
}
