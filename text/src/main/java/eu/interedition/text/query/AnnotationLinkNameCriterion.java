package eu.interedition.text.query;

import com.google.common.base.Function;
import eu.interedition.text.QName;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class AnnotationLinkNameCriterion implements Criterion {
  private final QName name;

  AnnotationLinkNameCriterion(QName name) {
    this.name = name;
  }

  public QName getName() {
    return name;
  }

  public static Function<AnnotationLinkNameCriterion, QName> TO_NAME = new Function<AnnotationLinkNameCriterion, QName>() {
    public QName apply(AnnotationLinkNameCriterion input) {
      return input.getName();
    }
  };
}
