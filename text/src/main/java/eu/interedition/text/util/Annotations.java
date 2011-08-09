package eu.interedition.text.util;

import com.google.common.base.Function;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import eu.interedition.text.Annotation;
import eu.interedition.text.QName;

import java.util.Comparator;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Annotations {
  public static Ordering<Annotation> DEFAULT_ORDERING = Ordering.from(new Comparator<Annotation>() {
    public int compare(Annotation o1, Annotation o2) {
      return o1.compareTo(o2);
    }
  });

  public static ComparisonChain compare(Annotation a, Annotation b) {
    return ComparisonChain.start()
            .compare(a.getRange(), b.getRange())
            .compare(a.getName(), b.getName());
  }

  public static final Function<Annotation, QName> NAME = new Function<Annotation, QName>() {
    public QName apply(Annotation input) {
      return input.getName();
    }
  };
}
