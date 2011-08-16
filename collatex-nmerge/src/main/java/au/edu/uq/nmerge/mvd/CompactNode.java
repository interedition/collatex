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

import java.util.BitSet;

/**
 * Store Node in when constructing variants
 *
 * @author desmond
 */
public class CompactNode {
  BitSet incoming, outgoing;
  int index;

  CompactNode(int index) {
    this.incoming = new BitSet();
    this.outgoing = new BitSet();
    this.index = index;
  }

  /**
   * Get the set of versions leaving this node
   *
   * @return a BitSet
   */
  BitSet getOutgoing() {
    return outgoing;
  }

  /**
   * Get the set of versions entering this node
   *
   * @return a BitSet
   */
  BitSet getIncoming() {
    return incoming;
  }

  /**
   * Find the set of versions this node lacks as incoming
   *
   * @return the difference outgoing - incoming
   */
  BitSet getWantsIncoming() {
    BitSet difference = new BitSet();
    difference.or(outgoing);
    difference.andNot(incoming);
    return difference;
  }

  /**
   * Compute the difference between incoming, outgoing
   *
   * @return the difference
   */
  BitSet getWantsOutgoing() {
    BitSet difference = new BitSet();
    difference.or(incoming);
    difference.andNot(outgoing);
    return difference;
  }

  /**
   * Get the index into the pairs vector: the index
   * at which this node starts
   *
   * @return an int
   */
  int getIndex() {
    return index;
  }

  /**
   * Add as incoming a set of versions
   *
   * @param versions the versions to add
   */
  void addIncoming(Match arc) {
    incoming.or(arc.versions);
  }

  /**
   * Add as outgoing a pair
   *
   * @param arc the pair from the MVD to add as outgoing
   */
  void addOutgoing(Match arc) {
    outgoing.or(arc.versions);
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("incoming: " + incoming.toString() + "\n");
    sb.append("outgoing: " + outgoing.toString() + "\n");
    sb.append("index: " + index);
    return sb.toString();
  }
}