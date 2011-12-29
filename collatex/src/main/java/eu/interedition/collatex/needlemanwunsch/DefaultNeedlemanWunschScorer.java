package eu.interedition.collatex.needlemanwunsch;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.graph.VariantGraphVertex;

import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class DefaultNeedlemanWunschScorer implements NeedlemanWunschScorer {

  private final Comparator<Token> comparator;

  public DefaultNeedlemanWunschScorer(Comparator<Token> comparator) {
    this.comparator = comparator;
  }

  @Override
  public float score(Set<VariantGraphVertex> vertices, Token token) {
    for (VariantGraphVertex vertex : vertices) {
      final Set<Token> tokens = vertex.tokens();
      Preconditions.checkArgument(!tokens.isEmpty(), "Vertex without tokens");
      if (comparator.compare(Iterables.getFirst(tokens, null), token) == 0) {
        return 1;
      }
    }
    return -1;
  }

  @Override
  public float gap() {
    return -1;
  }
}
