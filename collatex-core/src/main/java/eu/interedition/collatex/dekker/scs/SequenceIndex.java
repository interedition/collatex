package eu.interedition.collatex.dekker.scs;


import eu.interedition.collatex.skipgrams.NewSkipgram;
import eu.interedition.collatex.skipgrams.NormalizedSkipgram;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

/*
 * Author: Ronald Haentjens Dekker
 * date: 06-12-2018
 *
 * Based on earlier work on the token index from 2014. That was based on continuous sequences.
 * This index also adds discontinuous sequences.
 *
 * Also based on earlier work of the skipgram vocabulary
 *
 */
public class SequenceIndex {
    // TODO: for NewSkipgram an interface needs to be extracted!
    private SortedMap<IndexEntry, List<NewSkipgram>> index;

    public void addSkipgram(NewSkipgram skipgram) {
        // normalized the skipgram
        NormalizedSkipgram normalizedSkipgram = new NormalizedSkipgram(skipgram.getTokensNormalized(), "");
        // normalized skipgram should become an Sequence index entry
        this.index.computeIfAbsent(normalizedSkipgram, e -> new ArrayList<>()).add(skipgram);
    }


}
