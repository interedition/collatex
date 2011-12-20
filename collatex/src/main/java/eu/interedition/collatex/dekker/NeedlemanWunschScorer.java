package eu.interedition.collatex.dekker;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.graph.VariantGraphVertex;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface NeedlemanWunschScorer {

  float score(VariantGraphVertex a, Token b);

  float gap();
}
