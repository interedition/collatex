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
import com.google.common.base.Preconditions;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

/**
 * This version of search uses Knuth-Morris-Pratt search
 * algorithm. Not very fast perhaps but reliable. I am sick
 * of problems with Karp-Rabin search. The hash keeps overflowing.
 *
 * @author Desmond Schmidt
 */
public class KMPSearchState<T> {
  private final Ordering<T> ordering;
  List<T> pattern;
  Set<Witness> v;
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
  public KMPSearchState(Ordering<T> ordering, List<T> pattern, Set<Witness> v) {
    this.ordering = ordering;
    this.v = v;
    this.pattern = pattern;
    next = initNext(ordering, pattern);
  }

  /**
   * Constructor for cloning this object - useful for split.
   * Leave the versions empty
   *
   * @param ss the SearchState object to clone
   */
  private KMPSearchState(KMPSearchState ss, Set<Witness> v) {
    this.ordering = ss.ordering;
    this.pattern = ss.pattern;
    this.v = Sets.newHashSet(v);
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
  private static <T> int[] initNext(Ordering<T> ordering, List<T> pattern) {
    int[] next = new int[pattern.size()];
    int i = 0, j = -1;
    next[0] = -1;
    while (i < pattern.size() - 1) {
      while (j >= 0 && ordering.compare(pattern.get(i), pattern.get(j)) != 0)
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
   * @param v the witness set of which we might be a subset
   * @return true if we are a subset of bs
   */
  boolean isSubset(Set<Witness> v) {
    return v.containsAll(this.v);
  }

  /**
   * Combine the versions of the given search state with ours.
   *
   * @param s the search state object to merge with this one
   */
  void merge(KMPSearchState s) {
    this.v.addAll(s.v);
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
  KMPSearchState split(Set<Witness> bs) {
    final Sets.SetView<Witness> intersection = Sets.intersection(this.v, bs);
    Preconditions.checkArgument(!intersection.isEmpty());
    return new KMPSearchState(this, Sets.newHashSet(intersection));
  }

  /**
   * Update the search state with a new byte
   *
   * @param c the character from the text to update with
   * @return true if a match, false otherwise
   */
  boolean update(T c) {
    if (ordering.compare(pattern.get(pos), c) == 0)
    // we have a match
    {
      pos++;
      if (pos == pattern.size()) {
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
