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
 * Represent a node in the tree. Adapted from FastME C code.
 *
 * @author Desmond Schmidt
 */
public class node {
  String label;
  edge parentEdge;
  edge leftEdge;
  edge middleEdge;
  edge rightEdge;
  int index;
  int index2;

  /**
   * Create a node in the graph
   *
   * @param label      its label
   * @param index      its index in the matrix
   * @param parentEdge edge that leads to it?
   */
  node(String label, edge parentEdge, int index) {
    this.label = label;
    this.index = index;
    this.index2 = -1;
    this.parentEdge = parentEdge;
    this.leftEdge = null;
    this.middleEdge = null;
    this.rightEdge = null;
  }

  /**
   * Is this node a leaf?
   *
   * @return true if it is
   */
  boolean leaf() {
    int count = 0;
    if (null != parentEdge) {
      count++;
    }
    if (null != leftEdge) {
      count++;
    }
    if (null != rightEdge) {
      count++;
    }
    if (null != middleEdge) {
      count++;
    }
    if (count > 1) {
      return false;
    }
    return true;
  }
}
