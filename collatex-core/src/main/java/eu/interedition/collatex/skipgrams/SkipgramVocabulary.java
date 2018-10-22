package eu.interedition.collatex.skipgrams;

/*
 * Skipgram Vocabulary
 *
 * @author: Ronald Haentjens Dekker
 * @date: 22-10-2018
 *
 * Builds a skipgram vocabulary based on the normalized form of the skipgram
 *
 */

import eu.interedition.collatex.Token;
import eu.interedition.collatex.simple.SimpleToken;

import java.util.*;
import java.util.stream.Collectors;

public class SkipgramVocabulary {
    private SortedSet<NormalizedSkipgram> index;

    public SkipgramVocabulary() {
        this.index = new TreeSet<>(new NormalizedSkipgramComparator());
    }

    public void addSkipgrammedWitness(List<NormalizedSkipgram> skipgramList) {
        index.addAll(skipgramList);
    }


    public void addWitness(List<Token> tokens) {
        // create skipgrams
        SkipgramCreatorKerasStyle skipgramCreatorKerasStyle = new SkipgramCreatorKerasStyle();
        List<Skipgram> skipgramsWitness = skipgramCreatorKerasStyle.create(tokens, 2);
        // normalize them!
        List<NormalizedSkipgram> normalizedSkipgrams = skipgramsWitness.stream().map(sg -> new NormalizedSkipgram(((SimpleToken) sg.head).getNormalized(), ((SimpleToken) sg.tail).getNormalized())).collect(Collectors.toList());
        System.out.println(normalizedSkipgrams);
        this.addSkipgrammedWitness(normalizedSkipgrams);
    }



    public String toString() {
        return index.toString();
    }

    public int size() {
        return index.size();
    }
}
