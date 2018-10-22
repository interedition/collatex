package eu.interedition.collatex.skipgrams;

import java.util.Comparator;


/*
 * Normalized Skipgram Comparator
 * author: Ronald Haentjens Dekker
 * date: 22-10-2018
 *
 * Uses a String comparator to compare two normalized skipgrams to each other
 *
 */
public class NormalizedSkipgramComparator implements Comparator<NormalizedSkipgram> {

    @Override
    public int compare(NormalizedSkipgram o1, NormalizedSkipgram o2) {
        // We first compare token 1 of skipgram 1 and skipgram 2 with each other
        // if they are different we can immedialyu report it...
        // if not, we compare the second token of skipgram 1 and skipgram 2
        int compare = o1.head.compareTo(o2.head);
        if (compare != 0) return compare;
        compare = o1.tail.compareTo(o2.tail);
        return compare;
    }
}
