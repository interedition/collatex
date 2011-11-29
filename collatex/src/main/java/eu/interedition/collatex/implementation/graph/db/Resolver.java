package eu.interedition.collatex.implementation.graph.db;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface Resolver<T> {

  int add(T entity);

  T resolve(int ref);

  int resolve(T entity);
}
