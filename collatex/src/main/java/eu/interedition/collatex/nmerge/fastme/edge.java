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
package eu.interedition.collatex.nmerge.fastme;

/**
 * Represent an edge in the tree.
 * Comments are as supplied by Desper and Gascuel's original C version (2002).
 * http://www.ncbi.nlm.nih.gov/CBBresearch/Desper/FastME.html
 *
 * @author Desmond Schmidt 24/12/10
 */
public class edge {
  String label;
  node tail; //for edge (u,v), u is the tail, v is the head
  node head;
  int bottomsize; //number of nodes below edge
  int topsize;    //number of nodes above edge
  double distance;
  double totalweight;

  /**
   * Create an edge
   *
   * @param label  the edge's label
   * @param tail
   * @param head
   * @param weight
   */
  edge(String label, node tail, node head, double weight) {
    this.label = label;
    this.tail = tail;
    this.head = head;
    this.distance = weight;
    this.totalweight = 0.0;
  }

  /**
   * findBottomLeft searches by bottom down in the
   * tree and to the left.
   */
  edge findBottomLeft() {
    edge f = this;
    while (f.head.leftEdge != null) {
      f = f.head.leftEdge;
    }
    return f;
  }

  edge moveUpRight() {
    edge f = this;
    while ((null != f) && (f.tail.leftEdge != f)) {
      f = f.tail.parentEdge;
    }
    //go up the tree until f is a leftEdge
    if (null == f) {
      return f; //triggered at end of search
    } else {
      return (f.tail.rightEdge);
    }
    //and then go right
  }

  edge moveRight() {
    edge f;
    // this step moves from a left-oriented edge
    // to a right-oriented edge
    f = tail.rightEdge;
    if (null != f) {
      f = f.findBottomLeft();
    }
    return f;
  }

  edge siblingEdge() {
    if (this == tail.leftEdge) {
      return tail.rightEdge;
    } else {
      return tail.leftEdge;
    }
  }

  void updateSizes(direction d) {
    edge f;
    switch (d) {
      case UP:
        f = head.leftEdge;
        if (null != f) {
          f.updateSizes(direction.UP);
        }
        f = head.rightEdge;
        if (null != f) {
          f.updateSizes(direction.UP);
        }
        topsize++;
        break;
      case DOWN:
        f = siblingEdge();
        if (null != f) {
          f.updateSizes(direction.UP);
        }
        f = tail.parentEdge;
        if (null != f) {
          f.updateSizes(direction.DOWN);
        }
        bottomsize++;
        break;
    }
  }

  void assignBottomsize() {
    if (head.leaf()) {
      bottomsize = 1;
    } else {
      head.leftEdge.assignBottomsize();
      head.rightEdge.assignBottomsize();
      bottomsize = head.leftEdge.bottomsize
              + head.rightEdge.bottomsize;
    }
  }

  void assignTopsize(int numLeaves) {
    topsize = numLeaves - bottomsize;
    head.leftEdge.assignTopsize(numLeaves);
    head.rightEdge.assignTopsize(numLeaves);
  }

  /**
   * OLSint and OLSext use the average distance array to
   * calculate weights instead of using the edge average weight
   * fields
   */
  void OLSext(double[][] A) {
    edge f, g;
    if (head.leaf()) {
      f = siblingEdge();
      distance = 0.5 * (A[head.index][tail.index]
              + A[head.index][f.head.index]
              - A[f.head.index][tail.index]);
    } else {
      f = head.leftEdge;
      g = head.rightEdge;
      distance = 0.5 * (A[head.index][f.head.index]
              + A[head.index][g.head.index]
              - A[f.head.index][g.head.index]);
    }
  }

  double wf(double lambda, double D_LR, double D_LU, double D_LD,
            double D_RU, double D_RD, double D_DU) {
    double weight;
    weight = 0.5 * (lambda * (D_LU + D_RD) + (1 - lambda) * (D_LD + D_RU)
            - (D_LR + D_DU));
    return weight;
  }

  void OLSint(double[][] A) {
    double lambda;
    edge left, right, sib;
    left = head.leftEdge;
    right = head.rightEdge;
    sib = siblingEdge();
    lambda = ((double) sib.bottomsize * left.bottomsize +
            right.bottomsize * tail.parentEdge.topsize) /
            (bottomsize * topsize);
    distance = wf(lambda, A[left.head.index][right.head.index],
            A[left.head.index][tail.index],
            A[left.head.index][sib.head.index],
            A[right.head.index][tail.index],
            A[right.head.index][sib.head.index],
            A[sib.head.index][tail.index]);
  }

  /**
   * works except when e is the one edge inserted to new vertex
   * v by firstInsert
   */
  void WFext(double[][] A) {
    edge f, g;
    if (head.leaf() && tail.leaf()) {
      distance = A[head.index][head.index];
    } else if (head.leaf()) {
      f = tail.parentEdge;
      g = siblingEdge();
      distance = 0.5 * (A[head.index][g.head.index]
              + A[head.index][f.head.index]
              - A[g.head.index][f.head.index]);
    } else {
      f = head.leftEdge;
      g = head.rightEdge;
      distance = 0.5 * (A[g.head.index][head.index]
              + A[f.head.index][head.index]
              - A[f.head.index][g.head.index]);
    }
  }

  void WFint(double[][] A) {
    int up, down, left, right;
    up = tail.index;
    down = siblingEdge().head.index;
    left = head.leftEdge.head.index;
    right = head.rightEdge.head.index;
    distance = 0.25 * (A[up][left] + A[up][right]
            + A[left][down] + A[right][down])
            - 0.5 * (A[down][up] + A[left][right]);
  }

  /**
   * works except when e is the one edge inserted to new vertex
   * v by firstInsert
   */
  void BalWFext(double[][] A) {
    edge f, g;
    if (head.leaf() && tail.leaf()) {
      distance = A[head.index][head.index];
    } else if (head.leaf()) {
      f = tail.parentEdge;
      g = siblingEdge();
      distance = 0.5 * (A[head.index][g.head.index]
              + A[head.index][f.head.index]
              - A[g.head.index][f.head.index]);
    } else {
      f = head.leftEdge;
      g = head.rightEdge;
      distance = 0.5 * (A[g.head.index][head.index]
              + A[f.head.index][head.index]
              - A[f.head.index][g.head.index]);
    }
  }

  void BalWFint(double[][] A) {
    int up, down, left, right;
    up = tail.index;
    down = siblingEdge().head.index;
    left = head.leftEdge.head.index;
    right = head.rightEdge.head.index;
    distance = 0.25 * (A[up][left] + A[up][right]
            + A[left][down] + A[right][down])
            - 0.5 * (A[down][up] + A[left][right]);
  }
}
