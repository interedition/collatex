package eu.interedition.collatex.graph;

import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface EntityMapper<T> {

  Set<T> map(int... refs);

  int[] map(Set<T> entities);
}
