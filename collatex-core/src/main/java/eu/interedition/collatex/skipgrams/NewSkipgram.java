package eu.interedition.collatex.skipgrams;


import eu.interedition.collatex.Token;
import eu.interedition.collatex.simple.SimpleToken;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/*
 * @author: Ronald Haentjens Dekker
 * 15-11-2018
 * More flexible skipgrams...
 *
 */
public class NewSkipgram {
    List<Token> head;
    List<Token> tail;


    // andere mogelijkheden: is om met een start positie te werken
    // en een mask vector daar overheen te leggen...
    // dan heb je dus methoden nodig om de head terug te geven..



    public NewSkipgram(List<Token> head) {
        this.head = head;
        this.tail = Collections.EMPTY_LIST;
    }

    //Since this class is a value object it needs a hashcode!
    @Override
    public int hashCode() {
        return head.hashCode() + 7* tail.hashCode();
    }

    // and equals() and otherwise things go very wrong
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NewSkipgram)) {
            return false;
        }
        NewSkipgram other = (NewSkipgram) obj;
        return other.head.equals(this.head) && other.tail.equals(this.tail);
    }

    public String toString() {
        return head.toString()+" _ "+tail.toString();
    }


    public String getTokensNormalized() {
        // we first only take the head tokens
        List<String> normalized = this.head.stream().map(e -> ((SimpleToken)e).getNormalized()).collect(Collectors.toList());
        return normalized.toString();

    }
}
