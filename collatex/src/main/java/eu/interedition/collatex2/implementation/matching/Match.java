package eu.interedition.collatex2.implementation.matching;

import com.google.common.base.Function;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Match<T> {

  public final T left;
  public final T right;

  public Match(T left, T right) {
    this.left = left;
    this.right = right;
  }

  public Match<T> flip() {
    return new Match<T>(right, left);
  }

  public static <T> Function<Match<T>, Match<T>> flipFunction() {
    return new Function<Match<T>, Match<T>>() {
      @Override
      public Match<T> apply(Match<T> input) {
        return input.flip();
      }
    };
  }
}
