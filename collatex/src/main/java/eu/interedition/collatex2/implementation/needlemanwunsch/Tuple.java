package eu.interedition.collatex2.implementation.needlemanwunsch;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Tuple<T> {
  public final T a;
  public final T b;

  public Tuple(T a, T b) {
    this.a = a;
    this.b = b;
  }
}
