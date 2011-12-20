package eu.interedition.collatex.needlemanwunsch;

import com.google.common.base.Preconditions;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.graph.VariantGraphVertex;

import java.util.Comparator;
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
  public float score(VariantGraphVertex a, Token b) {
    final SortedSet<Token> tokens = a.tokens();
    Preconditions.checkArgument(!tokens.isEmpty(), "Vertex without tokens");
    return (comparator.compare(tokens.first(), b) == 0 ? 1 : -1);
  }

  @Override
  public float gap() {
    return -10;
  }
}
