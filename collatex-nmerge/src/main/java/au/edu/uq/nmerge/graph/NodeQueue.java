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
package au.edu.uq.nmerge.graph;

import java.util.Vector;

/**
 * A Queue of Nodes
 */
class NodeQueue<T> extends Vector<VariantGraphNode<T>> {
  static final long serialVersionUID = 1;

  /**
   * Add a Node to the queue. A bit difficult, since the node may
   * already be there. Check that, and prevent duplicates.
   *
   * @param node the node to push
   */
  void push(VariantGraphNode<T> node) {
    for (int i = size() - 1; i >= 0; i--) {
      if (get(i) == node) {
        return;
      }
    }
    add(node);
  }

  VariantGraphNode<T> pop() {
    VariantGraphNode<T> u = null;
    if (size() > 0) {
      u = remove(0);
    }
    return u;
  }
}
