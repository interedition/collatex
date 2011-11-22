package eu.interedition.collatex2.implementation.needlemanwunsch;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface Scorer<T> {

  float score(T a, T b);

  float gap();
}
