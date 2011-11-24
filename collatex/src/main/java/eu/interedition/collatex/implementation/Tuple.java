package eu.interedition.collatex.implementation;

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

  public Tuple<T> flip() {
    return new Tuple<T>(right, left);
  }

  public static <T> Function<Tuple<T>, Tuple<T>> flipFunction() {
    return new Function<Tuple<T>, Tuple<T>>() {
      @Override
      public Tuple<T> apply(Tuple<T> input) {
        return input.flip();
      }
    };
  }
}
