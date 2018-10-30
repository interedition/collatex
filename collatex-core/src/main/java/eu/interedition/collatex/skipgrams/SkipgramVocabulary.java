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
    private SortedMap<NormalizedSkipgram, Integer> index;

    public SkipgramVocabulary() {
        this.index = new TreeMap<>(new NormalizedSkipgramComparator());
    }

    public void addSkipgrammedWitness(List<NormalizedSkipgram> skipgramList) {
        // hoog de count een op voor elke normalized skipgram entry
        for (NormalizedSkipgram nskgr : skipgramList) {
            index.merge(nskgr, 1, (oldValue, one) -> oldValue + one);
        }
    }


    public void addWitness(List<Token> tokens) {
        // create skipgrams
        SkipgramCreator skipgramCreatorKerasStyle = new SkipgramCreator();
        List<Skipgram> skipgramsWitness = skipgramCreatorKerasStyle.create(tokens, 3);
        // normalize them!
        List<NormalizedSkipgram> normalizedSkipgrams = skipgramsWitness.stream().map(sg -> new NormalizedSkipgram(((SimpleToken) sg.head).getNormalized(), ((SimpleToken) sg.tail).getNormalized())).collect(Collectors.toList());
        this.addSkipgrammedWitness(normalizedSkipgrams);
    }


    /*
     * I am not sure you always want to do things like this
     * There could be multiple instances of a normalized skipgram.
     */
    public void removeNormalizedSkipgram(NormalizedSkipgram normalizedSkipgram) {
        index.remove(normalizedSkipgram);
    }



    public String toString() {
        return index.toString();
    }

    public int size() {
        return index.size();
    }

    //NOTE: Ik zou er ook een priority queue van kunnen maken..
    public NormalizedSkipgram selectHighestCount() {
//        System.out.println(index);
        // Ga alle map entries af, bewaar de hoogste max value.
        Optional<Map.Entry<NormalizedSkipgram, Integer>> max = index.entrySet().stream().max((e1, e2) -> e1.getValue().compareTo(e2.getValue()));
        if (!max.isPresent()) {
            throw new RuntimeException("what happened here?");
        }
        return max.get().getKey();
    }
}
