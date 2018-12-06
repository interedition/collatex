package eu.interedition.collatex.dekker.scs;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;

import java.util.List;

/*
 * Trying to create a new base class
 * That indexes using the new algorithm
 *
 */
public class SCSBasedDekkerAlgorithm extends CollationAlgorithm.Base  {
    @Override
    public void collate(VariantGraph against, Iterable<Token> witness) {
        throw new RuntimeException("Adding witness by witness is not allowed in this approach.");
    }

    /*
     * Provide multiple witnesses and align them...
     */
    @Override
    public void collate(VariantGraph against, List<? extends Iterable<Token>> witnesses) {
        System.out.println("This is work in progress and may crash at any time!");

        // the first step is to create a Token Array from the input
        // then in subsequent steps we can refer to tokens just by using a single integer..

        // The Token Array preparation code is part of the token index ...
        // Lets factor it out...

    }
}
