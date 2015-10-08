package eu.interedition.collatex.dekker;

import eu.interedition.collatex.CollationAlgorithm;

import java.util.List;

/**
 * Created by ronalddekker on 08/10/15.
 */
public interface InspectableCollationAlgorithm extends CollationAlgorithm {
    List<List<Match>> getPhraseMatches();

    List<List<Match>> getTranspositions();

    /*
         * This check disables transposition rendering in the variant
         * graph when the variant graph contains more then two witnesses.
         * Transposition detection is done in a progressive manner
         * (witness by witness). When viewing the resulting graph
         * containing the variation for all witnesses
         * the detected transpositions can look strange, since segments
         * may have split into smaller or larger parts.
         */
    void setMergeTranspositions(boolean b);
}
