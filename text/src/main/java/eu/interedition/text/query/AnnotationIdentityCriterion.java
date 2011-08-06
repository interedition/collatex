package eu.interedition.text.query;

import com.google.common.base.Function;
import eu.interedition.text.Annotation;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class AnnotationIdentityCriterion implements Criterion {
  private final Annotation annotation;

  AnnotationIdentityCriterion(Annotation annotation) {
    this.annotation = annotation;
  }

  public Annotation getAnnotation() {
    return annotation;
  }

  public static Function<AnnotationIdentityCriterion, Annotation> TO_ANNOTATION = new Function<AnnotationIdentityCriterion, Annotation>() {
    public Annotation apply(AnnotationIdentityCriterion input) {
      return input.getAnnotation();
    }
  };

}
