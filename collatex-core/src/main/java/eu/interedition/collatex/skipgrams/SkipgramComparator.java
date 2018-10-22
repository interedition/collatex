package eu.interedition.collatex.skipgrams;

import eu.interedition.collatex.Token;

import java.util.Comparator;


/*
 * Skipgram Comparator
 * author: Ronald Haentjens Dekker
 * date: 22-10-2018
 *
 * Uses an existing token comparator to compare two skipgrams to each other
 *
 */
public class SkipgramComparator implements Comparator<Skipgram> {

    private final Comparator<Token> tokenComparator;

    @Override
    public int compare(Skipgram o1, Skipgram o2) {
        // We first compare token 1 of skipgram 1 and skipgram 2 with each other
        // if they are different we can immedialyu report it...
        // if not, we compare the second token of skipgram 1 and skipgram 2
        int compare = tokenComparator.compare(o1.head, o2.head);
        if (compare != 0) return compare;
        compare = tokenComparator.compare(o2.tail, o2.tail);
        return compare;
    }

    public SkipgramComparator(Comparator<Token> tokenComparator) {
        this.tokenComparator = tokenComparator;
    }
}
