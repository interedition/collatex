package eu.interedition.text.util;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import eu.interedition.text.Annotation;
import eu.interedition.text.QName;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Annotations {
  public static int compare(Annotation a, Annotation b) {
    final int rangeComparison = a.getRange().compareTo(b.getRange());
    if (rangeComparison != 0) {
      return rangeComparison;
    }
    final int nameComparison = a.getName().compareTo(b.getName());
    if (nameComparison != 0) {
      return nameComparison;
    }
    return Ordering.arbitrary().compare(a, b);
  }

  public static final Function<Annotation, QName> NAME = new Function<Annotation, QName>() {
    public QName apply(Annotation input) {
      return input.getName();
    }
  };
}
