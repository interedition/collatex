package eu.interedition.collatex;

import eu.interedition.collatex.dekker.DekkerAlgorithm;
import eu.interedition.collatex.needlemanwunsch.NeedlemanWunschAlgorithm;

import java.util.Comparator;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class CollationAlgorithmFactory {
  
  public static CollationAlgorithm dekker(Comparator<Token> comparator) {
    return new DekkerAlgorithm(comparator);
  }
  
  public static CollationAlgorithm needlemanWunsch(Comparator<Token> comparator) {
    return new NeedlemanWunschAlgorithm(comparator);
  }
}
