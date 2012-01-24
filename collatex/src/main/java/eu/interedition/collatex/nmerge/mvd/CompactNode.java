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

package eu.interedition.collatex.nmerge.mvd;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Store Node in when constructing variants
 *
 * @author desmond
 */
public class CompactNode {
  Set<Witness> incoming;
  Set<Witness> outgoing;
  int index;

  CompactNode(int index) {
    this.incoming = Sets.newHashSet();
    this.outgoing = Sets.newHashSet();
    this.index = index;
  }

  /**
   * Get the set of versions leaving this node
   *
   * @return a BitSet
   */
  Set<Witness> getOutgoing() {
    return outgoing;
  }

  /**
   * Get the set of versions entering this node
   *
   * @return a BitSet
   */
  Set<Witness> getIncoming() {
    return incoming;
  }

  /**
   * Find the set of versions this node lacks as incoming
   *
   * @return the difference outgoing - incoming
   */
  Set<Witness> getWantsIncoming() {
    return Sets.newHashSet(Sets.difference(outgoing, incoming));
  }

  /**
   * Compute the difference between incoming, outgoing
   *
   * @return the difference
   */
  Set<Witness> getWantsOutgoing() {
    return Sets.newHashSet(Sets.difference(incoming, outgoing));
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
  void addIncoming(Match<?> arc) {
    incoming.addAll(arc.witnesses);
  }

  /**
   * Add as outgoing a pair
   *
   * @param arc the pair from the MVD to add as outgoing
   */
  void addOutgoing(Match<?> arc) {
    outgoing.addAll(arc.witnesses);
  }

  public String toString() {
    final Joiner joiner = Joiner.on(",");
    StringBuffer sb = new StringBuffer();
    sb.append("incoming: " + joiner.join(incoming) + "\n");
    sb.append("outgoing: " + joiner.join(outgoing) + "\n");
    sb.append("index: " + index);
    return sb.toString();
  }
}