package eu.interedition.text.query;

import eu.interedition.text.*;

import java.util.Arrays;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Criteria {
  public static Criterion any() {
    return new AnyCriterion();
  }

  public static Criterion none() {
    return new NoneCriterion();
  }

  public static Criterion is(Annotation annotation) {
    return new AnnotationIdentityCriterion(annotation);
  }

  public static Criterion annotationName(QName name) {
    return new AnnotationNameCriterion(name);
  }

  public static Criterion linkName(QName name) {
    return new AnnotationLinkNameCriterion(name);
  }

  public static Criterion text(Text text) {
    return new TextCriterion(text);
  }

  public static Criterion rangeOverlap(Range range) {
    return new RangeOverlapCriterion(range);
  }

  public static Criterion rangeLength(int length) {
    return new RangeLengthCriterion(length);
  }

  public static Operator and(Criterion... criteria) {
    return new AndOperator(Arrays.asList(criteria));
  }

  public static Operator or(Criterion... criteria) {
    return new OrOperator(Arrays.asList(criteria));
  }

  public static Criterion not(Criterion criterion) {
    return new NotOperator(criterion);
  }
}
