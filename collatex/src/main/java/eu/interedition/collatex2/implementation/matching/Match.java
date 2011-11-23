package eu.interedition.collatex2.implementation.matching;

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
}
