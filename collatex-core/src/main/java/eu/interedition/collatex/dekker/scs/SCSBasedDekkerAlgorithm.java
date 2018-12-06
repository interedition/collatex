package eu.interedition.collatex.dekker.scs;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.skipgrams.NewSkipgram;
import eu.interedition.collatex.skipgrams.NormalizedSkipgram;
import eu.interedition.collatex.skipgrams.SkipgramCreator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

        SequenceIndex sequenceIndex = new SequenceIndex();

        // we first fill the sequence index with discontinuous sequences aka skipgrams..
        SkipgramCreator creator = new SkipgramCreator();

        for (Iterable<Token> witnessTokens : witnesses) {
            //TODO sequence is not the best name; because there are all the tokens of the witness
            List<Token> sequence = new ArrayList<>();
            witnessTokens.forEach(sequence::add);
            List<NewSkipgram> skipgrams = creator.thirdCreate(sequence, 3, 5);
            // now for each skipgram we have to check whether the normalized form already exists in the sequence index.
            // if not add it ...

        }




//        // the first step is to create a Token Array from the input
//        // then in subsequent steps we can refer to tokens just by using a single integer..
//
//        // The Token Array preparation code is part of the token index ...
//        // Lets factor it out...
//        TokenArray tokenArray = new TokenArray();
//        Token[] token_Array = tokenArray.prepareTokenArray(witnesses);
//
//        // now we want to find subsequences based on this token array

    }

    // als we hier nu eens een enkele voudige methode van maken op de sequence index

    // er zijn meerdere entries in the value
    // dus moet werken met compute if absent
    // verder kan het tellen in de oude index dan gewoon met een size method call on the list
//    public void addSkipgrammedWitnessNewStyle(List<NewSkipgram> skipgramList) {
//        List<NormalizedSkipgram> normalizedSkipgrams = skipgramList.stream().map(sg -> new NormalizedSkipgram(sg.getTokensNormalized(), "")).collect(Collectors.toList());
//        for (int i = 0; i< normalizedSkipgrams.size(); i++) {
//            NormalizedSkipgram normalizedSkipgram = normalizedSkipgrams.get(i);
//            NewSkipgram newSkipgram = skipgramList.get(i);
//            this.newIndex.computeIfAbsent(normalizedSkipgram, e -> new ArrayList<>()).add(newSkipgram);
//        }
//    }


}
