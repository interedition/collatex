package eu.interedition.collatex.graph;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.MapEvictionListener;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class DefaultResolver<T> implements Resolver<T> {

  private int nextKey = 0;
  private final Map<T, Integer> keys;
  private final Map<Integer, T> values = Maps.newHashMap();

  public DefaultResolver() {
    this(1, TimeUnit.DAYS);
  }

  public DefaultResolver(long expiryHours) {
    this(expiryHours, TimeUnit.HOURS);
  }

  public DefaultResolver(long expiryDuration, TimeUnit expiryTimeUnit) {
    keys = new MapMaker()
            .concurrencyLevel(4)
            .expireAfterWrite(expiryDuration, expiryTimeUnit)
            .evictionListener(new MapEvictionListener<Object, Object>() {
              @Override
              public void onEviction(Object key, Object value) {
                values.remove(value);
              }
            })
            .makeComputingMap(new Function<T, Integer>() {
              @Override
              public Integer apply(T input) {
                int result = nextKey++;
                values.put(result, input);
                return result;
              }
            });
  }

  @Override
  public Set<T> resolve(int... refs) {
    final Set<T> resolved = Sets.newHashSetWithExpectedSize(refs.length);
    for (int rc = 0; rc < refs.length; rc++) {
      resolved.add(Preconditions.checkNotNull(values.get(refs[rc])));
    }
    return resolved;
  }

  @Override
  public int[] resolve(Set<T> entities) {
    final int[] refs = new int[entities.size()];
    int ec = 0;
    for (T entity : entities) {
      refs[ec++] = keys.get(entity);
    }
    return refs;
  }
}
