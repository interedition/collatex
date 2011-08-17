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
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Set;

/**
 * Represent a variant computed from a range in a base version
 *
 * @author Desmond Schmidt 1/6/09
 */
public class Variant<T> implements Comparable<Variant<T>> {
  /**
   * more than one version for a variant is possible
   */
  Set<Witness> versions;
  /**
   * the start-index where it occurs within pairs
   */
  int startIndex;
  /**
   * end index within pairs (used for isWithin)
   */
  int endIndex;
  /**
   * the start-offset within start node
   */
  int startOffset;
  /**
   * the length of the variant's real data in bytes
   */
  int length;
  /**
   * the mvd it is associated with
   */
  Collation<T> collation;
  /**
   * the actual data of this variant
   */
  List<T> data;

  /**
   * Construct a variant
   *
   * @param startOffset initial offset within startIndex
   * @param startIndex  the index within mvd of the first node
   * @param length      the length of the variant
   * @param versions    the set of versions over the variant
   * @param collation   the mvd it came from
   * @throws MVDException
   */
  public Variant(int startOffset, int startIndex, int endIndex,
                 int length, Set<Witness> versions, Collation<T> collation) {
    this.startIndex = startIndex;
    this.endIndex = endIndex;
    this.collation = collation;
    this.versions = versions;
    this.length = length;
    this.startOffset = startOffset;
    findContent();
  }

  /**
   * Get the version set
   *
   * @return a BitSet
   */
  public Set<Witness> getVersions() {
    return versions;
  }

  /**
   * Create a String representing the header part of a chunk
   *
   * @return the header as a String including the trailing ':'
   */
  protected String createHeader() {
    StringBuffer sb = new StringBuffer();
    sb.append('[');
    sb.append(Joiner.on(",").join(versions));
    sb.append(':');
    return sb.toString();
  }

  /**
   * Convert to a string
   */
  public String toString() {
    String header = createHeader();
    StringBuffer sb = new StringBuffer();
    sb.append(Iterables.toString(data));
    String dataStr = sb.toString();
    return header + dataStr + "]";
  }

  /**
   * Test for equality. Versions don't matter. What we want is to find
   * out if two variants have the same text.
   *
   * @param other the other variant to compare with this one
   * @return true if they are the same
   */
  public boolean equals(Object other) {
    Variant<?> otherV = (Variant<?>) other;
    return this.versions.equals(otherV.versions)
            && this.startIndex == otherV.startIndex
            && this.endIndex == otherV.endIndex
            && this.startOffset == otherV.startOffset
            && this.collation == otherV.collation
            && this.equalsContent(otherV);
  }

  /**
   * Are two variants equal in content but differ only in versions?
   *
   * @param other the other variant to compare with
   * @return true if they are 'equal'
   */
  public boolean equalsContent(Variant<?> other) {
    if (!this.collation.equals(other.collation))
      return false;
    else {
      return this.data.equals(other.data);
    }
  }

  /**
   * Generate a hash of the content of this Variant. It should be almost
   * unique. It will be used to collect together and wipe out any variants
   * generated during the getApparatus method that are identical.
   */
  public int hashCode() {
    return Objects.hashCode(startIndex, startOffset, versions, data);
  }

  /**
   * Generate content by following the paths of the variant
   * in the MVD.
   */
  private void findContent() {
    data = Lists.newArrayList();
    int iNode = startIndex;
    Match<T> p = collation.getMatches().get(iNode);
    int i = startOffset;
    int totalLen = 0;
    while (p.length() == 0 || totalLen < this.length) {
      if (p.length() == 0 || i == p.length()) {
        iNode = collation.next(iNode + 1, Iterables.getFirst(versions, null));
        p = collation.getMatches().get(iNode);
        i = 0;
      } else {
        data.addAll(p.getData());
        totalLen += p.getData().size();
      }
    }
  }

  /**
   * Merge two variants equal in content
   *
   * @param other the other variant to merge with this one.
   */
  public void merge(Variant<T> other) {
    this.versions.addAll(other.versions);
  }

  /**
   * Is this variant entirely contained within another variant?
   * We just check if we are within the bounds of the other variant.
   * No need to compare the text of the two variants - the versions
   * must be the same, so within the bounds means that the same text
   * will occur.
   *
   * @param other the other variant to compare it to
   * @return true if we are within other, false otherwise
   */
  public boolean isWithin(Variant<T> other) {
    // these tests will mostly fail
    // so we can avoid the main computation
    if (length < other.length
            && startIndex >= other.startIndex
            && endIndex <= other.endIndex
            && this.versions.equals(other.versions)) {
      // another quick test to shortcut the computation
      if (startIndex == other.startIndex
              && (startOffset < other.startOffset
              || (startOffset - other.startOffset) + length > other.length))
        return false;
      else {
        // OK, we have some work to do ...
        // find the start of this variant in other
        int offset = other.startOffset;
        int index = other.startIndex;
        Match<T> p = collation.getMatches().get(index);
        int i = 0;
        Witness followV = Iterables.getFirst(versions, null);
        while (i < other.length) {
          if (offset == p.length()) {
            index = collation.next(index + 1, followV);
            p = collation.getMatches().get(index);
            offset = 0;
          } else {
            offset++;
            i++;
          }
          // found start?
          if (index == startIndex && offset == startOffset)
            return other.length - i >= length;
        }
      }
    }
    return false;
  }

  /**
   * Compare two Variants. Try to short-circuit the
   * comparison to reduce computation.
   *
   * @param other the variant to compare ourselves to
   */
  public int compareTo(Variant<T> other) {
    if (this.startIndex < other.startIndex)
      return -1;
    else if (this.startIndex > other.startIndex)
      return 1;
    else if (this.startOffset < other.startOffset)
      return -1;
    else if (this.startOffset > other.startOffset)
      return 1;
    else if (this.length < other.length)
      return -1;
    else if (this.length > other.length)
      return 1;
    else {
      // FIXME: What is a proper ordering of witness sets?
      final Joiner joiner = Joiner.on(',');
      String thisV = joiner.join(versions);
      String thatV = joiner.join(other.versions);
      int res = thisV.compareTo(thatV);
      if (res != 0)
        return res;
      else {
        // FIXME: introduce token comparator to do a proper comparison
        return Iterables.toString(data).compareTo(Iterables.toString(other.data));
      }
    }
  }
}
