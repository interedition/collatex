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

import com.sun.org.apache.bcel.internal.generic.NEW;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.simple.SimpleToken;

import java.util.*;
import java.util.stream.Collectors;

public class SkipgramVocabulary {
    // the old index
    private SortedMap<NormalizedSkipgram, Integer> index;

    // the new index
    // maps a normalized version to the actual skipgrams in the witnesses
    private SortedMap<NormalizedSkipgram, List<NewSkipgram>> newIndex;


    public SkipgramVocabulary() {
        this.index = new TreeMap<>(new NormalizedSkipgramComparator());
        this.newIndex = new TreeMap<>(new NormalizedSkipgramComparator());
    }

    // er zijn meerdere entries in the value
    // dus moet werken met compute if absent
    // verder kan het tellen in de oude index dan gewoon met een size method call on the list
    public void addSkipgrammedWitnessNewStyle(List<NewSkipgram> skipgramList) {
        List<NormalizedSkipgram> normalizedSkipgrams = skipgramList.stream().map(sg -> new NormalizedSkipgram(sg.getTokensNormalized(), "")).collect(Collectors.toList());
        for (int i = 0; i< normalizedSkipgrams.size(); i++) {
            NormalizedSkipgram normalizedSkipgram = normalizedSkipgrams.get(i);
            NewSkipgram newSkipgram = skipgramList.get(i);
            this.newIndex.computeIfAbsent(normalizedSkipgram, e -> new ArrayList<>()).add(newSkipgram);
        }
    }

    // oud
    public void addSkipgrammedWitness(List<NormalizedSkipgram> skipgramList) {
        // hoog de count een op voor elke normalized skipgram entry
        for (NormalizedSkipgram nskgr : skipgramList) {
            index.merge(nskgr, 1, (oldValue, one) -> oldValue + one);
        }
    }


    // oud
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
        if (this.index.isEmpty()) {
            return this.newIndex.toString();
        }
        return index.toString();
    }

    public int size() {
        if (this.index.isEmpty()) {
            return this.newIndex.size();
        }
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
