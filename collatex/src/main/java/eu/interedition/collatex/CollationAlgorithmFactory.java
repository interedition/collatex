package eu.interedition.collatex;

import eu.interedition.collatex.dekker.VariantGraphBuilder;

import java.util.Comparator;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class CollationAlgorithmFactory {
  
  public static CollationAlgorithm dekker(Comparator<Token> comparator) {
    return new VariantGraphBuilder(comparator);
  }
}
