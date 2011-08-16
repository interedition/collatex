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
package au.edu.uq.nmerge.graph.suffixtree;

import com.google.common.base.Preconditions;


/**
 * This structure describes a node and its incoming edge
 */
public class SuffixTreeNode {
  /**
   * A linked list of sons of that node
   */
  SuffixTreeNode children;
  /**
   * A linked list of right siblings of that node
   */
  SuffixTreeNode rightSibling;
  /**
   * A linked list of left siblings of that node
   */
  SuffixTreeNode leftSibling;
  /**
   * A pointer to that node's father
   */
  SuffixTreeNode parent;
  /**
   * A pointer to the node that represents the largest
   * suffix of the current node
   */
  SuffixTreeNode largestSuffix;
  /**
   * Index of the start position of the node's path
   */
  int pathPosition;
  /**
   * Start index of the incoming edge
   */
  int edgeLabelStart;
  /**
   * End index of the incoming edge
   */
  int edgeLabelEnd;

  public SuffixTreeNode() {
  }

  /**
   * Create a Node
   *
   * @param parent   father of the node
   * @param start    the starting index of the incoming edge to that node
   * @param end      the end index of the incoming edge to that node
   * @param position the path starting position of the node.
   */
  public SuffixTreeNode(SuffixTreeNode parent, int start, int end, int position) {
    Preconditions.checkArgument(end >= start, "Error: start greater than end");
    this.parent = parent;
    this.pathPosition = position;
    this.edgeLabelStart = start;
    this.edgeLabelEnd = end;
  }

  /**
   * Does this node have no children, i.e. is it a leaf?
   *
   * @return true if this node is a leaf
   */
  public boolean isLeaf() {
    return children == null;
  }
}
