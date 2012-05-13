package eu.interedition.collatex;

import java.util.Comparator;

import eu.interedition.collatex.dekker.DekkerAlgorithm;
import eu.interedition.collatex.dekker.EditGraphTokenLinker;
import eu.interedition.collatex.graph.GraphFactory;
import eu.interedition.collatex.matrix.MatchMatrixLinker;
import eu.interedition.collatex.needlemanwunsch.NeedlemanWunschAlgorithm;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class CollationAlgorithmFactory {

  public static CollationAlgorithm dekker(Comparator<Token> comparator) {
    return new DekkerAlgorithm(comparator);
  }

  public static CollationAlgorithm dekkerExperimental(Comparator<Token> comparator, GraphFactory graphFactory) {
    return new DekkerAlgorithm(comparator, new EditGraphTokenLinker(graphFactory));
  }

  public static CollationAlgorithm dekkerMatchMatrix(Comparator<Token> comparator) {
    return new DekkerAlgorithm(comparator, new MatchMatrixLinker());
  }

  public static CollationAlgorithm needlemanWunsch(Comparator<Token> comparator) {
    return new NeedlemanWunschAlgorithm(comparator);
  }
}
