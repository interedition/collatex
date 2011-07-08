package eu.interedition.text;

import java.util.Map;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface AnnotationDataRepository {
  void set(Annotation annotation, Map<QName, String> data);

  void set(Annotation annotation, QName name, String value);

  Map<QName, String> get(Annotation annotation);

  String get(Annotation annotation, QName name);

  void delete(Annotation annotation, Set<QName> names);

  void delete(Annotation annotation, QName name);
}
