package eu.interedition.collatex.subst;

import java.util.Comparator;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.CollationAlgorithmFactory;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;

public class NeedlemanWunschForLayeredWitnessesAlgorithm extends CollationAlgorithm.Base {

    private static final Comparator<Token> LAYEREDTOKENCOMPARATOR = (t1, t2) -> ((LayerToken) t1).getContent().trim()//
            .compareToIgnoreCase(((LayerToken) t2).getContent().trim());

    @Override
    public void collate(VariantGraph against, Iterable<Token> witness) {
        CollationAlgorithm needlemanWunsch = CollationAlgorithmFactory.needlemanWunsch(LAYEREDTOKENCOMPARATOR);
        needlemanWunsch.collate(against, witness);
    }

}
