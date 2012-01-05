package eu.interedition.collatex.input;

import com.google.common.base.Preconditions;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.graph.Resolver;

import java.util.Map;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SimpleWitnessResolver implements Resolver<Witness> {
  private Map<Integer, SimpleWitness> witnesses = new MapMaker().concurrencyLevel(4).softValues().makeMap();

  @Override
  public Set<Witness> resolve(int... refs) {
    final Set<Witness> resolved = Sets.newHashSetWithExpectedSize(refs.length);
    for (int ref : refs) {
      resolved.add(Preconditions.checkNotNull(witnesses.get(ref)));
    }
    return resolved;
  }

  @Override
  public int[] resolve(Set<Witness> entities) {
    int[] resolved = new int[entities.size()];
    int rc = 0;
    for (Witness witness : entities) {
      final SimpleWitness simpleWitness = (SimpleWitness) witness;
      final int id = simpleWitness.getId();
      witnesses.put(id, simpleWitness);
      resolved[rc++] = id;
    }
    return resolved;
  }
}
