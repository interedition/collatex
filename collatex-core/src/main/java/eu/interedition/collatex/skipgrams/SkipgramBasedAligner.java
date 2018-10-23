package eu.interedition.collatex.skipgrams;

import eu.interedition.collatex.Token;

import java.util.List;

/*
 * Skipgram based aligner
 *
 * @author: Ronald Haentjens Dekker
 * @date: 23-10-2018
 *
 * test class to align three witnesses in an order independent manner.
 */
public class SkipgramBasedAligner {
    public SkipgramBasedAligner() {
        // hebben we state nodig? Geen idee
    }


    public void align(List<Token> witness1, List<Token> witness2, List<Token> witness3) {
        // We build a skipgram vocabulary based on the three witnesses.
        SkipgramVocabulary vocabulary = new SkipgramVocabulary();
        vocabulary.addWitness(witness1);
        vocabulary.addWitness(witness2);
        vocabulary.addWitness(witness3);
        NormalizedSkipgram normalizedSkipgram = vocabulary.selectHighestCount();
        System.out.println("We start with: "+normalizedSkipgram);
    }
}
