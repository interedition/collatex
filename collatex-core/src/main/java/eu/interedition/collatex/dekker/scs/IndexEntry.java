package eu.interedition.collatex.dekker.scs;

import eu.interedition.collatex.dekker.token_index.Block;

import java.util.List;


/*
 * @author: Ronald Haentjens Dekker
 * @date: 6-12-2018; based on earlier work done in 2014.
 *
 * Interface for all the entries in the Sequence Index
 *
 * Originally created for the Suffix Array / LCP intervals approach
 * It now needs to work for both continues and discontinuous sequences.
 *
 */
public interface IndexEntry {
    // TODO: maybe move method to sequence index?
    // the number of witnesses in which this index entry occurs
    int getDepth();

    // TODO: maybe move method to sequence index?
    // frequency = number of times this block of text occurs in complete witness set
    int getFrequency();

    // the number of tokens that the sequence in this entry contains
    int getLength();

    // All the actual occurrences of this entry.
    // TODO: needs to be renamed
    // Could also be moved to the sequence index class itself..
    List<Block.Instance> getAllInstances();

    // get normalized form of sequence of this entry
    String getNormalized();

}
