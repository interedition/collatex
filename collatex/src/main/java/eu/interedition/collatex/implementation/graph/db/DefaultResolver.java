package eu.interedition.collatex.implementation.graph.db;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class DefaultResolver<T> implements Resolver<T> {

  private final BiMap<T, Integer> entities = HashBiMap.create();

  @Override
  public int add(T entity) {
    if (entities.containsKey(entity)) {
      return entities.get(entity);
    } else {
      entities.put(entity, entities.size());
      return entities.size() - 1;
    }
  }

  @Override
  public T resolve(int ref) {
    return entities.inverse().get(ref);
  }

  @Override
  public int resolve(T entity) {
    return entities.get(entity);
  }
}
