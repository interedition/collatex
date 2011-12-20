package eu.interedition.collatex.dekker;

import com.google.common.base.Function;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Tuple<T> {

  public final T left;
  public final T right;

  public Tuple(T left, T right) {
    this.left = left;
    this.right = right;
  }
}
