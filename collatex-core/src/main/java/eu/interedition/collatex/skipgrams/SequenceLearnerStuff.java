package eu.interedition.collatex.skipgrams;


import eu.interedition.collatex.Token;

import java.util.List;

/*
 * @author: Ronald Haentjens Dekker
 * @date: 26-11-2018
 *
 * Stupid attempt to learn about the sequences using small as possible n-grams with a possible skip if needed.
 * We want to do two things:
 * We want the smallest n-grams and smallest skips that are still unique within one witness
 * We want the largest possible amount of overlap between the witnesses.
 *
 * In the first iteration, we create all the unigrams for all the sequences ... we then map them to a normalized
 * version...
 */
public class SequenceLearnerStuff {

    public void addWitness(List<Token> witnessTokens) {
        SkipgramCreator creator = new SkipgramCreator();
        // start with bigrams and trigrams with variable skip
        List<NewSkipgram> newSkipgrams = creator.thirdCreate(witnessTokens, 1, 5);
        System.out.println(newSkipgrams);



//        SkipgramVocabulary vocabulary = new SkipgramVocabulary();
//        vocabulary.addSkipgrammedWitnessNewStyle(newSkipgrams);
//        System.out.println(vocabulary.toString());



    }
//    SkipgramVocabulary vocabulary = new SkipgramVocabulary();
//        vocabulary.addWitness(witness1);
//        vocabulary.addWitness(witness2);
//        vocabulary.addWitness(witness3);

}
