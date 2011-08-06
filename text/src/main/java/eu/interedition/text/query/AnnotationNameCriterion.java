package eu.interedition.text.query;

import com.google.common.base.Function;
import eu.interedition.text.QName;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class AnnotationNameCriterion implements Criterion {
  private final QName name;

  AnnotationNameCriterion(QName name) {
    this.name = name;
  }

  public QName getName() {
    return name;
  }

  public static Function<AnnotationNameCriterion, QName> TO_NAME = new Function<AnnotationNameCriterion, QName>() {
    public QName apply(AnnotationNameCriterion input) {
      return input.getName();
    }
  };

}
