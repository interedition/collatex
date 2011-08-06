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

  void set(Map<Annotation, Map<QName, String>> data);

  void set(Annotation annotation, Map<QName, String> data);

  void set(Annotation annotation, QName name, String value);

  Map<QName, String> get(Annotation annotation);

  String get(Annotation annotation, QName name);

  void delete(Annotation annotation, Set<QName> names);

  void delete(Annotation annotation, QName name);
}
