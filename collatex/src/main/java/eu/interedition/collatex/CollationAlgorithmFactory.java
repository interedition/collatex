package eu.interedition.collatex;

import java.util.Comparator;

import eu.interedition.collatex.dekker.DefaultTokenLinker;
import eu.interedition.collatex.dekker.DekkerAlgorithm;
import eu.interedition.collatex.dekker.EditGraphTokenLinker;
import eu.interedition.collatex.dekker.matrix.MatchTableLinker;
import eu.interedition.collatex.graph.GraphFactory;
import eu.interedition.collatex.needlemanwunsch.NeedlemanWunschAlgorithm;

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

  @Deprecated
  public static CollationAlgorithm dekkerPreviousVersion(Comparator<Token> comparator) {
   return new DekkerAlgorithm(comparator, new DefaultTokenLinker());
  }
    
  @Deprecated
  public static CollationAlgorithm dekkerExperimental(Comparator<Token> comparator, GraphFactory graphFactory) {
    return new DekkerAlgorithm(comparator, new EditGraphTokenLinker(graphFactory));
  }
}
