package eu.interedition.collatex.implementation.graph.db;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

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
  public Set<T> resolve(int... refs) {
    final Set<T> resolved = Sets.newHashSetWithExpectedSize(refs.length);
    final BiMap<Integer, T> inverseMapping = entities.inverse();
    for (int rc = 0; rc < refs.length; rc++) {
      resolved.add(inverseMapping.get(refs[rc]));
    }
    return resolved;
  }

  @Override
  public int[] resolve(Set<T> entities) {
    final int[] refs = new int[entities.size()];
    int ec = 0;
    for (T entity : entities) {
      refs[ec++] = add(entity);
    }
    return refs;
  }
}
