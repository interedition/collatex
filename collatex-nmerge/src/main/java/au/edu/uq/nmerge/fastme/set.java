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
package au.edu.uq.nmerge.fastme;

/**
 * A linked-list structure to hold nodes.
 * Also adapted from FastME C code.
 */
class set {
  node node;
  set next;

  set(node v) {
    this.node = v;
    this.next = null;
  }

  /**
   * Add to the list
   *
   * @param v the node to add
   */
  void addToSet(node v) {
    set current = this;
    while (current.next != null)
      current = current.next;
    current.next = new set(v);
  }
}
