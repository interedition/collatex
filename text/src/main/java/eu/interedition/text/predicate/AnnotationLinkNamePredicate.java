package eu.interedition.text.predicate;

import com.google.common.base.Function;
import eu.interedition.text.QName;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class AnnotationLinkNamePredicate implements AnnotationLinkPredicate {
  private final QName name;

  public AnnotationLinkNamePredicate(QName name) {
    this.name = name;
  }

  public QName getName() {
    return name;
  }

  public static Function<AnnotationLinkNamePredicate, QName> TO_NAME = new Function<AnnotationLinkNamePredicate, QName>() {
    public QName apply(AnnotationLinkNamePredicate input) {
      return input.getName();
    }
  };
}
