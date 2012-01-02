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

package eu.interedition.collatex.nmerge.graph;

import eu.interedition.collatex.nmerge.mvd.Witness;

import java.util.Set;

/**
 * We need to store information about which versions each
 * previous character in the graph belongs to. This is
 * because previous characters (as used in the
 * MatchThreadDirect.isMaximal() routine)
 *
 * @author desmond
 */
public class PrevChar<T> {
  /**
   * the previous byte to some starting-point
   * of a match in the graph
   */
  T previous;
  /**
   * set of version that this byte belongs
   * to (the versions of its arc)
   */
  Set<Witness> versions;

  /**
   * Vanilla constructor
   *
   * @param versions the versions of the arc the prevchar
   *                 belongs to
   * @param previous the previous byte
   */
  PrevChar(Set<Witness> versions, T previous) {
    this.versions = versions;
    this.previous = previous;
  }
}
