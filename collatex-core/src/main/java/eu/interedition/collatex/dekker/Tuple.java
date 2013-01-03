package eu.interedition.collatex.dekker;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Tuple<T> {

  public final T left;
  public final T right;

  private final Set<T> set;

  public Tuple(T left, T right) {
    this.left = left;
    this.right = right;
    this.set = Sets.newHashSet(left, right);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Tuple) {
      return set.equals(((Tuple) obj).set);
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return set.hashCode();
  }
}
