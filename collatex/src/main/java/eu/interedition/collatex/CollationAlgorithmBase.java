package eu.interedition.collatex;

import com.google.common.collect.Sets;
import eu.interedition.collatex.graph.VariantGraph;

import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public abstract class CollationAlgorithmBase implements CollationAlgorithm {

  protected abstract void collate(VariantGraph against, SortedSet<Token> witness);
  
  @Override
  public void collate(VariantGraph against, Iterable<Token> witness) {
    collate(against, Sets.newTreeSet(witness));
  }

  @Override
  public void collate(VariantGraph against, Iterable<Token>... witnesses) {
    collate(against, Arrays.asList(witnesses));
  }

  @Override
  public void collate(VariantGraph against, List<Iterable<Token>> witnesses) {
    for (Iterable<Token> witness : witnesses) {
      collate(against, witness);
    }
  }
}
