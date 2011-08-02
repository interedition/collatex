package eu.interedition.text;

import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface AnnotationPredicate {
  Set<Annotation> annotationEquals();

  Set<Text> annotationAnnotates();

  Set<QName> annotationhasName();

  Set<Range> annotationOverlaps();
}
