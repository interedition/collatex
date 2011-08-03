package eu.interedition.text.predicate;

import com.google.common.base.Function;
import eu.interedition.text.Annotation;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class AnnotationIdentityPredicate implements AnnotationPredicate {
  private final Annotation annotation;

  public AnnotationIdentityPredicate(Annotation annotation) {
    this.annotation = annotation;
  }

  public Annotation getAnnotation() {
    return annotation;
  }

  public static Function<AnnotationIdentityPredicate, Annotation> TO_ANNOTATION = new Function<AnnotationIdentityPredicate, Annotation>() {
    public Annotation apply(AnnotationIdentityPredicate input) {
      return input.getAnnotation();
    }
  };

}
