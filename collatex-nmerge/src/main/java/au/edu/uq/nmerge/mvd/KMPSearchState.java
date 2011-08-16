/*
 * NMerge is Copyright 2009-2011 Desmond Schmidt
 *
 * This file is part of NMerge. NMerge is a Java library for merging
 * multiple versions into multi-version documents (MVDs), and for
 * reading, searching and comparing them.
 *
 * NMerge is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package au.edu.uq.nmerge.mvd;

import au.edu.uq.nmerge.exception.MVDException;

import java.util.BitSet;

/**
 * This version of search uses Knuth-Morris-Pratt search
 * algorithm. Not very fast perhaps but reliable. I am sick
 * of problems with Karp-Rabin search. The hash keeps overflowing.
 *
 * @author Desmond Schmidt
 */
public class KMPSearchState {
  byte[] pattern;
  BitSet v;
  KMPSearchState following;
  int[] next;
  /**
   * next byte to match
   */
  int pos;

  /**
   * Initialisation is easy.
   *
   * @param pattern the pattern to search for
   */
  public KMPSearchState(byte[] pattern, BitSet v) {
    this.v = v;
    this.pattern = pattern;
    next = initNext(pattern);
  }

  /**
   * Constructor for cloning this object - useful for split.
   * Leave the versions empty
   *
   * @param ss the SearchState object to clone
   */
  private KMPSearchState(KMPSearchState ss, BitSet bs) {
    this.pattern = ss.pattern;
    this.v = new BitSet();
    this.v.or(bs);
    this.pos = ss.pos;
    next = new int[ss.next.length];
    for (int i = 0; i < ss.next.length; i++)
      this.next[i] = ss.next[i];
  }

  /**
   * Initialise the next table
   *
   * @param pattern the pattern as a byte array in any encoding
   * @return an array of next indices
   */
  private static int[] initNext(byte[] pattern) {
    int[] next = new int[pattern.length];
    int i = 0, j = -1;
    next[0] = -1;
    while (i < pattern.length - 1) {
      while (j >= 0 && pattern[i] != pattern[j])
        j = next[j];
      i++;
      j++;
      next[i] = j;
    }
    next[0] = 0;
    return next;
  }

  /**
   * Concatenate a list of SearchState objects to the end of our list.
   *
   * @param list a list of SearchState objects
   */
  void append(KMPSearchState list) {
    KMPSearchState temp = this;
    while (temp.following != null)
      temp = temp.following;
    temp.following = list;
  }

  /**
   * Override of the Object method
   *
   * @param obj another SearchState object to compare with
   * @return true if they have the same internal states but
   *         different sets
   */
  public boolean equals(Object obj) {
    return ((KMPSearchState) obj).pos == pos;
  }

  /**
   * Are this object's versions a subset of those given?
   *
   * @param bs the BitSet of which we might be a subset
   * @return true if we are a subset of bs
   */
  boolean isSubset(BitSet bs) {
    int i;
    for (i = v.nextSetBit(0); i >= 0; i = v.nextSetBit(i + 1)) {
      if (bs.nextSetBit(i) != i)
        break;
    }
    return i == -1;
  }

  /**
   * Combine the versions of the given search state with ours.
   *
   * @param s the search state object to merge with this one
   */
  void merge(KMPSearchState s) {
    v.or(s.v);
  }

  /**
   * Remove a SearchState object from the list of which we are a part.
   * The object must be in the list FROM the point at which we are at
   * (because we are not doubly-linked).
   *
   * @param item the list item to remove
   * @return the list with the item removed (may be null)
   * @throws MVDException
   */
  KMPSearchState remove(KMPSearchState item) throws MVDException {
    KMPSearchState previous, list, temp;
    previous = temp = list = this;
    while (temp != null && temp != item) {
      previous = temp;
      temp = temp.following;
    }
    if (previous == temp)    // it matched immediately
    {
      list = temp.following;    // could be null!
      temp.following = null;
    } else if (temp == null)    // it didn't find it!
      throw new MVDException("List item not found");
    else                    // temp in the middle of the list
    {
      previous.following = temp.following;
      temp.following = null;
    }
    return list;
  }

  /**
   * Split off a clone of ourselves intersecting with bs as its set of
   * versions. Should only be called after this.v.intersects(bs) has
   * returned true.
   *
   * @param bs the set which must intersect with our versions.
   * @return a clone of everything we stand for.
   */
  KMPSearchState split(BitSet bs) {
    BitSet newBs = new BitSet();
    for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
      if (v.nextSetBit(i) == i) {
        // move each bit in v & bs to newBs
        v.clear(i);
        newBs.set(i);
      }
    }
    return new KMPSearchState(this, newBs);
  }

  /**
   * Update the search state with a new byte
   *
   * @param c the character from the text to update with
   * @return true if a match, false otherwise
   */
  boolean update(byte c) {
    if (pattern[pos] == c)
    // we have a match
    {
      pos++;
      if (pos == pattern.length) {
        pos = 0;
        return true;
      }
    } else
      // we have a mismatch -
      // use the next array to reset pos
      pos = next[pos];
    return false;
  }
}
