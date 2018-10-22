package eu.interedition.collatex.skipgrams;

import eu.interedition.collatex.Token;

/*
 * Author: Ronald Haentjens Dekker
 * Date: 22-10-2018
 *
 * Simple tuple that represents a normalized skipgram.
 * Value object
 */
public class NormalizedSkipgram {
    String head;
    String tail;

    public NormalizedSkipgram(String head, String tail) {
        this.head = head;
        this.tail = tail;
    }

    //Since this class is a value object it needs a hashcode!
    @Override
    public int hashCode() {
        return head.hashCode() + 7* tail.hashCode();
    }

    // and equals() and otherwise things go very wrong
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NormalizedSkipgram)) {
            return false;
        }
        NormalizedSkipgram other = (NormalizedSkipgram) obj;
        return other.head.equals(this.head) && other.tail.equals(this.tail);
    }

    public String toString() {
        return "N"+head+","+tail;
    }

}
