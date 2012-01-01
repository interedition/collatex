package eu.interedition.collatex.graph;

import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface Resolver<T> {

  Set<T> resolve(int... refs);

  int[] resolve(Set<T> entities);
}
