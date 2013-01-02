package eu.interedition.collatex;

import eu.interedition.collatex.dekker.DekkerAlgorithm;
import eu.interedition.collatex.dekker.matrix.MatchTableLinker;
import eu.interedition.collatex.needlemanwunsch.NeedlemanWunschAlgorithm;

import java.util.Comparator;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 * @author Ronald Haentjens Dekker
 */
public class CollationAlgorithmFactory {

  public static CollationAlgorithm dekker(Comparator<Token> comparator) {
    return dekkerMatchMatrix(comparator, 3);
  }

  public static CollationAlgorithm dekkerMatchMatrix(Comparator<Token> comparator, int outlierTranspositionsSizeLimit) {
    return new DekkerAlgorithm(comparator, new MatchTableLinker(outlierTranspositionsSizeLimit));
  }

  public static CollationAlgorithm needlemanWunsch(Comparator<Token> comparator) {
    return new NeedlemanWunschAlgorithm(comparator);
  }
}
