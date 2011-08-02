package eu.interedition.text.util;

import eu.interedition.text.*;

import java.util.Set;

import static java.util.Collections.singleton;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SimpleAnnotationPredicate implements AnnotationPredicate {
  public static final SimpleAnnotationPredicate ANY = new SimpleAnnotationPredicate(null, null, null, null);

  private final Set<Annotation> equals;
  private final Set<Text> annotated;
  private final Set<QName> named;
  private final Set<Range> overlaps;

  public SimpleAnnotationPredicate(Set<Annotation> equals, Set<Text> annotated, Set<QName> named, Set<Range> overlaps) {
    this.equals = equals;
    this.annotated = annotated;
    this.named = named;
    this.overlaps = overlaps;
  }

  public SimpleAnnotationPredicate(Text annotated, QName named) {
    this(null, singleton(annotated), singleton(named), null);
  }

  public SimpleAnnotationPredicate(Text annotated) {
    this(null, singleton(annotated), null, null);
  }

  public Set<Annotation> annotationEquals() {
    return equals;
  }

  public Set<Text> annotationAnnotates() {
    return annotated;
  }

  public Set<QName> annotationhasName() {
    return named;
  }

  public Set<Range> annotationOverlaps() {
    return overlaps;
  }

  public static boolean isNullOrEmpty(Set<?> set) {
    return (set == null || set.isEmpty());
  }

  public static boolean isAny(AnnotationPredicate p) {
    return isNullOrEmpty(p.annotationEquals()) &&
            isNullOrEmpty(p.annotationAnnotates()) &&
            isNullOrEmpty(p.annotationhasName()) &&
            isNullOrEmpty(p.annotationOverlaps());
  }
}
