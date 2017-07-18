package eu.interedition.collatex.dekker;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.dekker.editgraphaligner.EditGraphAligner;
import eu.interedition.collatex.dekker.island.Island;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Created by bramb on 26-6-2017.
 */
public class DekkerAlgorithm extends EditGraphAligner implements InspectableCollationAlgorithm {
    public DekkerAlgorithm() {
        super();
    }

    public DekkerAlgorithm(Comparator<Token> comparator) {
        super(comparator);
    }

    public Set<Island> getAllPossibleIslands() {
        throw new UnsupportedOperationException();
    }

    public List<Island> getPreferredIslands() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<List<Match>> getPhraseMatches() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<List<Match>> getTranspositions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMergeTranspositions(boolean b) {
        throw new UnsupportedOperationException();
    }
}
