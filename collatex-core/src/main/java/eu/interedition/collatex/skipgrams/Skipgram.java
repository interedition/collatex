package eu.interedition.collatex.skipgrams;

import eu.interedition.collatex.Token;

/*
 * Author: Ronald Haentjens Dekker
 * Date: 19-10-2018
 *
 * Simple tuple that represents a skipgram.
 * Value object
 */
public class Skipgram {
    Token head;
    Token tail;

    public Skipgram(Token head, Token tail) {
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
        if (!(obj instanceof  Skipgram)) {
            return false;
        }
        Skipgram other = (Skipgram) obj;
        return other.head.equals(this.head) && other.tail.equals(this.tail);
    }

    public String toString() {
        return head.toString()+" _ "+tail.toString();
    }

}
