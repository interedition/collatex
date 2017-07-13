package eu.interedition.collatex.dekker;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.dekker.editgraphaligner.EditGraphAligner;
import eu.interedition.collatex.dekker.island.Island;

import java.util.*;

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
        return new HashSet<>();
    }

    public List<Island> getPreferredIslands() {
        return new ArrayList<>();
    }

    @Override
    public List<List<Match>> getPhraseMatches() {
        return new ArrayList<>();
    }

    @Override
    public List<List<Match>> getTranspositions() {
        return new ArrayList<>();
    }

    @Override
    public void setMergeTranspositions(boolean b) {

    }
}
