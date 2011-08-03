package eu.interedition.text.predicate;

import com.google.common.base.Function;
import eu.interedition.text.QName;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class AnnotationNamePredicate implements AnnotationPredicate {
  private final QName name;

  public AnnotationNamePredicate(QName name) {
    this.name = name;
  }

  public QName getName() {
    return name;
  }

  public static Function<AnnotationNamePredicate, QName> TO_NAME = new Function<AnnotationNamePredicate, QName>() {
    public QName apply(AnnotationNamePredicate input) {
      return input.getName();
    }
  };

}
