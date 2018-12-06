package eu.interedition.collatex.skipgrams;

import eu.interedition.collatex.dekker.scs.IndexEntry;
import eu.interedition.collatex.dekker.token_index.Block;

import java.util.List;

/*
 * Author: Ronald Haentjens Dekker
 * Date: 22-10-2018
 *
 * Simple tuple that represents a normalized skipgram.
 * Value object
 */
public class NormalizedSkipgram implements IndexEntry {
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
        return head;
    }

    @Override
    public int getDepth() {
        return 0;
    }

    @Override
    public int getFrequency() {
        return 0;
    }

    @Override
    public int getLength() {
        return 0;
    }

    @Override
    public List<Block.Instance> getAllInstances() {
        return null;
    }

    @Override
    public String getNormalized() {
        return head;
    }

}
