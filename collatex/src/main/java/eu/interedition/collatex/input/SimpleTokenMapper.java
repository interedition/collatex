package eu.interedition.collatex.input;

import com.google.common.base.Preconditions;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.graph.EntityMapper;

import java.util.Map;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SimpleTokenMapper implements EntityMapper<Token> {
  private Map<Integer, SimpleToken> tokens = new MapMaker().concurrencyLevel(4).softValues().makeMap();

  @Override
  public Set<Token> map(int... refs) {
    final Set<Token> resolved = Sets.newHashSetWithExpectedSize(refs.length);
    for (int ref : refs) {
      resolved.add(Preconditions.checkNotNull(tokens.get(ref)));
    }
    return resolved;
  }

  @Override
  public int[] map(Set<Token> entities) {
    int[] resolved = new int[entities.size()];
    int rc = 0;
    for (Token token : entities) {
      final SimpleToken simpleToken = (SimpleToken) token;
      final int id = simpleToken.getId();
      tokens.put(id, simpleToken);
      resolved[rc++] = id;
    }
    return resolved;
  }
}
