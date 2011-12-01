package eu.interedition.collatex.implementation.graph.db;

import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface Resolver<T> {

  int add(T entity);

  Set<T> resolve(int... refs);

  int[] resolve(Set<T> entities);
}
