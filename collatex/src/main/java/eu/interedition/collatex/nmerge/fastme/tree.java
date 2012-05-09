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
 * Represent a tree.
 * Code is translated from the original C version of Desper and 
 * Gascuel (2002). Should really be split into two subclasses of 
 * tree: GMETree and BMETree. 
 * http://www.ncbi.nlm.nih.gov/CBBresearch/Desper/FastME.html
 * I haven't added any new comments because I didn't write it
 * @author Desmond Schmidt 24/12/10
 */

import java.io.PrintStream;
import java.text.NumberFormat;

/**
 * Holds an entire tree of nodes
 */
public class tree {
//  String name;
  node root;
  int size;
  double weight;
  static int DOUBLE_PRECISION = 6;

  /**
   * Create a new tree
   */
  tree() {
    this.root = null;
    this.weight = -1;
    this.size = 0;
  }

  /**
   * topFirstTraverse starts from the top of T, and from
   * there moves stepwise down, left before right. assumes
   * tree has been detrifurcated
   */
  edge topFirstTraverse(edge e) {
    if (e == null)
    //first Edge searched
    {
      return this.root.leftEdge;
    } else if (!e.head.leaf())
    //down and to the left is preferred
    {
      return e.head.leftEdge;
    } else //e.head is a leaf
    {
      return e.moveUpRight();
    }
  }

  /**
   * depthFirstTraverse returns the edge f which is least in T
   * according to the depth-first order, but which is later than
   * e in the search pattern.  If e is null, f is the least edge of T
   */
  edge depthFirstTraverse(edge e) {
    edge f;
    if (e == null) {
      f = this.root.leftEdge;
      if (f != null) {
        f = f.findBottomLeft();
      }
      //this is the first edge of this search pattern
      return f;
    } else //e is non-null
    {
      if (e.tail.leftEdge == e)
      // if e is a left-oriented edge, we skip the entire
      // tree cut below e, and find least edge
      {
        f = e.moveRight();
      } else
      //if e is a right-oriented edge, we have already looked at its
      // sibling and everything below e, so we move up
      {
        f = e.tail.parentEdge;
      }
    }
    return f;
  }

  private void BMEcalcDownAverage(node v, edge e, double[][] D, double[][] A) {
    edge left, right;
    if (e.head.leaf()) {
      A[e.head.index][v.index] = D[v.index2][e.head.index2];
    } else {
      left = e.head.leftEdge;
      right = e.head.rightEdge;
      A[e.head.index][v.index] = 0.5 * A[left.head.index][v.index]
              + 0.5 * A[right.head.index][v.index];
    }
  }

  private void BMEcalcUpAverage(node v, edge e, double[][] D, double[][] A) {
    edge up, down;
    if (this.root == e.tail) {
      A[v.index][e.head.index] = D[v.index2][e.tail.index2];
    }
    // for now, use convention
    // v.index first => looking up
    // v.index second => looking down
    else {
      up = e.tail.parentEdge;
      down = e.siblingEdge();
      A[v.index][e.head.index] = 0.5 * A[v.index][up.head.index]
              + 0.5 * A[down.head.index][v.index];
    }
  }

  private void BMEcalcNewvAverages(node v, double[][] D, double[][] A) {
    //loop over edges
    //depth-first search
    edge e = null;
    // the downward averages need to be
    // calculated from bottom to top
    e = depthFirstTraverse(e);
    while (e != null) {
      BMEcalcDownAverage(v, e, D, A);
      e = depthFirstTraverse(e);
    }
    //the upward averages need to be calculated
    // from top to bottom
    e = topFirstTraverse(e);
    while (e != null) {
      BMEcalcUpAverage(v, e, D, A);
      e = topFirstTraverse(e);
    }
  }

  void GMEcalcUpAverage(node v, edge e, double[][] D, double[][] A) {
    edge up, down;
    if (null == e.tail.parentEdge) {
      A[v.index][e.head.index] = D[v.index2][e.tail.index2];
    } else {
      up = e.tail.parentEdge;
      down = e.siblingEdge();
      A[v.index][e.head.index] =
              (up.topsize * A[v.index][up.head.index] +
                      down.bottomsize * A[down.head.index][v.index])
                      / e.topsize;
    }
  }

  private void GMEcalcDownAverage(node v, edge e, double[][] D,
                                  double[][] A) {
    edge left, right;
    if (e.head.leaf()) {
      A[e.head.index][v.index] = D[v.index2][e.head.index2];
    } else {
      left = e.head.leftEdge;
      right = e.head.rightEdge;
      A[e.head.index][v.index] =
              (left.bottomsize * A[left.head.index][v.index] +
                      right.bottomsize * A[right.head.index][v.index])
                      / e.bottomsize;
    }
  }

  /**
   * this function calculates average distance D_Xv for each X
   * which is a set of leaves of an induced subtree of T
   */
  void GMEcalcNewvAverages(node v, double[][] D, double[][] A) {
    //loop over edges
    //depth-first search
    edge e = null;
    //the downward averages need to be
    //calculated from bottom to top
    e = depthFirstTraverse(e);
    while (null != e) {
      GMEcalcDownAverage(v, e, D, A);
      e = depthFirstTraverse(e);
    }
    //the upward averages need to be calculated
    // from top to bottom
    e = topFirstTraverse(e);
    while (null != e) {
      GMEcalcUpAverage(v, e, D, A);
      e = topFirstTraverse(e);
    }
  }

  private double wf4(double lambda, double lambda2, double D_AB, double D_AC,
                     double D_BC, double D_Av, double D_Bv, double D_Cv) {
    return (((1 - lambda) * (D_AC + D_Bv) + (lambda2 - 1) * (D_AB + D_Cv)
            + (lambda - lambda2) * (D_BC + D_Av)));
  }

  /**
   * Calculate what the OLS weight would be if v were inserted into
   * T along e.  Compare against known values for inserting along
   * f = e.parentEdge. edges are tested by a top-first, left-first
   * scheme. we presume all distances are fixed to the correct weight
   * for e.parentEdge, if e is a left-oriented edge
   */
  void testEdge(edge e, node v, double[][] A) {
    double lambda, lambda2;
    edge par, sib;
    sib = e.siblingEdge();
    par = e.tail.parentEdge;
    /*C is set above e.tail, B is set below e, and A is set below sib*/
    /*following the nomenclature of Desper & Gascuel*/
    lambda = (((double) (sib.bottomsize + e.bottomsize * par.topsize))
            / ((1 + par.topsize) * (par.bottomsize)));
    lambda2 = (((double) (sib.bottomsize + e.bottomsize * par.topsize))
            / ((1 + e.bottomsize) * (e.topsize)));
    e.totalweight = par.totalweight
            + wf4(lambda, lambda2, A[e.head.index][sib.head.index],
            A[sib.head.index][e.tail.index],
            A[e.head.index][e.tail.index],
            A[sib.head.index][v.index], A[e.head.index][v.index],
            A[v.index][e.tail.index]);
  }

  /**
   * The ME version of updateAveragesMatrix does not update the
   * entire matrix A, but updates A[v.index][w.index] whenever
   * this represents an average of 1-distant or 2-distant subtrees
   */
  void GMEupdateAveragesMatrix(double[][] A, edge e, node v, node newNode) {
    edge sib, par, left, right;
    sib = e.siblingEdge();
    left = e.head.leftEdge;
    right = e.head.rightEdge;
    par = e.tail.parentEdge;

    /*we need to update the matrix A so all 1-distant, 2-distant, and
         3-distant averages are correct*/

    /*first, initialize the newNode entries*/
    /*1-distant*/
    A[newNode.index][newNode.index] =
            (e.bottomsize * A[e.head.index][e.head.index]
                    + A[v.index][e.head.index])
                    / (e.bottomsize + 1);
    /*1-distant for v*/
    A[v.index][v.index] =
            (e.bottomsize * A[e.head.index][v.index]
                    + e.topsize * A[v.index][e.head.index])
                    / (e.bottomsize + e.topsize);

    /*2-distant for v,newNode*/
    A[v.index][newNode.index] = A[newNode.index][v.index] =
            A[v.index][e.head.index];

    /*second 2-distant for newNode*/
    A[newNode.index][e.tail.index] = A[e.tail.index][newNode.index]
            = (e.bottomsize * A[e.head.index][e.tail.index]
            + A[v.index][e.tail.index]) / (e.bottomsize + 1);
    /*third 2-distant for newNode*/
    A[newNode.index][e.head.index] = A[e.head.index][newNode.index]
            = A[e.head.index][e.head.index];

    if (null != sib) {
      /*fourth and last 2-distant for newNode*/
      A[newNode.index][sib.head.index] =
              A[sib.head.index][newNode.index] =
                      (e.bottomsize * A[sib.head.index][e.head.index]
                              + A[sib.head.index][v.index]) / (e.bottomsize + 1);
      updateSubTreeAverages(A, sib, v, direction.SKEW); /*updates sib and below*/
    }
    if (null != par) {
      if (e.tail.leftEdge == e) {
        updateSubTreeAverages(A, par, v, direction.LEFT); /*updates par and above*/
      } else {
        updateSubTreeAverages(A, par, v, direction.RIGHT);
      }
    }
    if (null != left) {
      updateSubTreeAverages(A, left, v, direction.UP); /*updates left and below*/
    }
    if (null != right) {
      updateSubTreeAverages(A, right, v, direction.UP); /*updates right and below*/
    }

    /*1-dist for e.head*/
    A[e.head.index][e.head.index] =
            (e.topsize * A[e.head.index][e.head.index]
                    + A[e.head.index][v.index]) / (e.topsize + 1);
    /*2-dist for e.head (v,newNode,left,right)
         taken care of elsewhere*/
    /*3-dist with e.head either taken care of elsewhere (below)
         or unchanged (sib,e.tail)*/

    /*symmetrize the matrix (at least for distant-2 subtrees) */
    A[v.index][e.head.index] = A[e.head.index][v.index];
    /*and distant-3 subtrees*/
    A[e.tail.index][v.index] = A[v.index][e.tail.index];
    if (null != left) {
      A[v.index][left.head.index] = A[left.head.index][v.index];
    }
    if (null != right) {
      A[v.index][right.head.index] = A[right.head.index][v.index];
    }
    if (null != sib) {
      A[v.index][sib.head.index] = A[sib.head.index][v.index];
    }

  }

  private void updateSubTreeAverages(double[][] A, edge e, node v,
                                     direction d)
  // the monster function of this program
  {
    edge sib, left, right, par;
    left = e.head.leftEdge;
    right = e.head.rightEdge;
    sib = e.siblingEdge();
    par = e.tail.parentEdge;
    switch (d) {
      //want to preserve correctness of
      //all 1-distant, 2-distant, and 3-distant averages
      //1-distant updated at edge splitting the two trees
      //2-distant updated:
      //(left.head,right.head) and (head,tail) updated at
      //a given edge.  Note, NOT updating (head,sib.head)!
      //(That would lead to multiple updating).
      //3-distant updated: at edge in center of quartet
      case UP: //point of insertion is above e
        // 1-distant average of nodes below e to
        // nodes above e
        A[e.head.index][e.head.index] =
                (e.topsize * A[e.head.index][e.head.index] +
                        A[e.head.index][v.index]) / (e.topsize + 1);
        //2-distant average of nodes below e to
        //nodes above parent of e
        A[e.head.index][par.head.index] =
                A[par.head.index][e.head.index] =
                        (par.topsize * A[par.head.index][e.head.index]
                                + A[e.head.index][v.index]) / (par.topsize + 1);
        //must do both 3-distant averages involving par
        if (null != left) {
          // and recursive call
          updateSubTreeAverages(A, left, v, direction.UP);
          //3-distant average
          A[par.head.index][left.head.index]
                  = A[left.head.index][par.head.index]
                  = (par.topsize * A[par.head.index][left.head.index]
                  + A[left.head.index][v.index]) / (par.topsize + 1);
        }
        if (null != right) {
          updateSubTreeAverages(A, right, v, direction.UP);
          A[par.head.index][right.head.index]
                  = A[right.head.index][par.head.index]
                  = (par.topsize * A[par.head.index][right.head.index]
                  + A[right.head.index][v.index]) / (par.topsize + 1);
        }
        break;
      case SKEW: //point of insertion is skew to e
        // 1-distant average of nodes below e to
        // nodes above e
        A[e.head.index][e.head.index] =
                (e.topsize * A[e.head.index][e.head.index] +
                        A[e.head.index][v.index]) / (e.topsize + 1);
        //no 2-distant averages to update in this case
        //updating 3-distant averages involving sib
        if (null != left) {
          updateSubTreeAverages(A, left, v, direction.UP);
          A[sib.head.index][left.head.index]
                  = A[left.head.index][sib.head.index]
                  = (sib.bottomsize * A[sib.head.index][left.head.index]
                  + A[left.head.index][v.index]) / (sib.bottomsize + 1);
        }
        if (null != right) {
          updateSubTreeAverages(A, right, v, direction.UP);
          A[sib.head.index][right.head.index]
                  = A[right.head.index][sib.head.index]
                  = (sib.bottomsize * A[par.head.index][right.head.index]
                  + A[right.head.index][v.index]) / (sib.bottomsize + 1);
        }
        break;
      case LEFT: //point of insertion is below the edge left
        //1-distant average
        A[e.head.index][e.head.index] =
                (e.bottomsize * A[e.head.index][e.head.index] +
                        A[v.index][e.head.index]) / (e.bottomsize + 1);
        // 2-distant averages
        A[e.head.index][e.tail.index] =
                A[e.tail.index][e.head.index] =
                        (e.bottomsize * A[e.head.index][e.tail.index] +
                                A[v.index][e.tail.index]) / (e.bottomsize + 1);
        A[left.head.index][right.head.index] =
                A[right.head.index][left.head.index] =
                        (left.bottomsize * A[right.head.index][left.head.index]
                                + A[right.head.index][v.index]) / (left.bottomsize + 1);
        //3-distant avereages involving left
        if (null != sib) {
          updateSubTreeAverages(A, sib, v, direction.SKEW);
          A[left.head.index][sib.head.index]
                  = A[sib.head.index][left.head.index]
                  = (left.bottomsize * A[left.head.index][sib.head.index]
                  + A[sib.head.index][v.index]) / (left.bottomsize + 1);
        }
        if (null != par) {
          if (e.tail.leftEdge == e) {
            updateSubTreeAverages(A, par, v, direction.LEFT);
          } else {
            updateSubTreeAverages(A, par, v, direction.RIGHT);
          }
          A[left.head.index][par.head.index]
                  = A[par.head.index][left.head.index]
                  = (left.bottomsize * A[left.head.index][par.head.index]
                  + A[v.index][par.head.index]) / (left.bottomsize + 1);
        }
        break;
      case RIGHT: //point of insertion is below the edge right
        //1-distant average
        A[e.head.index][e.head.index] =
                (e.bottomsize * A[e.head.index][e.head.index] +
                        A[v.index][e.head.index]) / (e.bottomsize + 1);
        //2-distant averages
        A[e.head.index][e.tail.index] =
                A[e.tail.index][e.head.index] =
                        (e.bottomsize * A[e.head.index][e.tail.index] +
                                A[v.index][e.tail.index]) / (e.bottomsize + 1);
        A[left.head.index][right.head.index] =
                A[right.head.index][left.head.index] =
                        (right.bottomsize * A[right.head.index][left.head.index]
                                + A[left.head.index][v.index]) / (right.bottomsize + 1);
        //3-distant avereages involving right
        if (null != sib) {
          updateSubTreeAverages(A, sib, v, direction.SKEW);
          A[right.head.index][sib.head.index]
                  = A[sib.head.index][right.head.index]
                  = (right.bottomsize * A[right.head.index][sib.head.index]
                  + A[sib.head.index][v.index]) / (right.bottomsize + 1);
        }
        if (null != par) {
          if (e.tail.leftEdge == e) {
            updateSubTreeAverages(A, par, v, direction.LEFT);
          } else {
            updateSubTreeAverages(A, par, v, direction.RIGHT);
          }
          A[right.head.index][par.head.index]
                  = A[par.head.index][right.head.index]
                  = (right.bottomsize * A[right.head.index][par.head.index]
                  + A[v.index][par.head.index]) / (right.bottomsize + 1);
        }
        break;
    }
  }

  private void GMEsplitEdge(node v, edge e, double[][] A) {
    String nodelabel;
    String edgelabel;
    edge newPendantEdge;
    edge newInternalEdge;
    node newNode;

    nodelabel = "I" + (this.size + 1);
    newNode = new node(nodelabel, null, this.size + 1);
    edgelabel = "E" + this.size;
    newPendantEdge = new edge(edgelabel, newNode, v, 0.0);
    edgelabel = "E" + (this.size + 1);
    newInternalEdge = new edge(edgelabel, newNode, e.head, 0.0);
    //update the matrix of average distances
    //also updates the bottomsize, topsize fields

    GMEupdateAveragesMatrix(A, e, v, newNode);

    newNode.parentEdge = e;
    e.head.parentEdge = newInternalEdge;
    v.parentEdge = newPendantEdge;
    e.head = newNode;

    this.size = this.size + 2;

    if (e.tail.leftEdge == e) {
      newNode.leftEdge = newInternalEdge;
      newNode.rightEdge = newPendantEdge;
    } else {
      newNode.leftEdge = newInternalEdge;
      newNode.rightEdge = newPendantEdge;
    }

    /*assign proper topsize, bottomsize values to the two new Edges*/

    newPendantEdge.bottomsize = 1;
    newPendantEdge.topsize = e.bottomsize + e.topsize;

    newInternalEdge.bottomsize = e.bottomsize;
    newInternalEdge.topsize = e.topsize;  /*off by one, but we adjust
						    that below*/

    /*and increment these fields for all other edges*/
    newInternalEdge.updateSizes(direction.UP);
    e.updateSizes(direction.DOWN);
  }

  void updatePair(double[][] A, edge nearEdge, edge farEdge, node v,
                  node root, double dcoeff, direction d) {
    edge sib;
    //the various cases refer to where the new vertex has
    //been inserted, in relation to the edge nearEdge
    switch (d) {
      case UP:
        //this case is called when v has been inserted above
        //or skew to farEdge
        //do recursive calls first!
        if (null != farEdge.head.leftEdge) {
          updatePair(A, nearEdge, farEdge.head.leftEdge, v, root, dcoeff,
                  direction.UP);
        }
        if (null != farEdge.head.rightEdge) {
          updatePair(A, nearEdge, farEdge.head.rightEdge, v, root,
                  dcoeff, direction.UP);
        }
        A[farEdge.head.index][nearEdge.head.index] =
                A[nearEdge.head.index][farEdge.head.index]
                        = A[farEdge.head.index][nearEdge.head.index]
                        + dcoeff * A[farEdge.head.index][v.index]
                        - dcoeff * A[farEdge.head.index][root.index];
        break;
      case DOWN: //called when v has been inserted below farEdge
        if (null != farEdge.tail.parentEdge) {
          updatePair(A, nearEdge, farEdge.tail.parentEdge, v, root,
                  dcoeff, direction.DOWN);
        }
        sib = farEdge.siblingEdge();
        if (null != sib) {
          updatePair(A, nearEdge, sib, v, root, dcoeff, direction.UP);
        }
        A[farEdge.head.index][nearEdge.head.index] =
                A[nearEdge.head.index][farEdge.head.index]
                        = A[farEdge.head.index][nearEdge.head.index]
                        + dcoeff * A[v.index][farEdge.head.index]
                        - dcoeff * A[farEdge.head.index][root.index];
    }
  }

  void updateSubTree(double[][] A, edge nearEdge, node v, node root,
                     node newNode, double dcoeff, direction d) {
    edge sib;
    switch (d) {
      case UP: //newNode is above the edge nearEdge
        A[v.index][nearEdge.head.index] = A[nearEdge.head.index][v.index];
        A[newNode.index][nearEdge.head.index] =
                A[nearEdge.head.index][newNode.index] =
                        A[nearEdge.head.index][root.index];
        if (null != nearEdge.head.leftEdge) {
          updateSubTree(A, nearEdge.head.leftEdge, v, root, newNode,
                  0.5 * dcoeff, direction.UP);
        }
        if (null != nearEdge.head.rightEdge) {
          updateSubTree(A, nearEdge.head.rightEdge, v, root, newNode,
                  0.5 * dcoeff, direction.UP);
        }
        updatePair(A, nearEdge, nearEdge, v, root, dcoeff, direction.UP);
        break;
      case DOWN: //newNode is below the edge nearEdge
        A[nearEdge.head.index][v.index] = A[v.index][nearEdge.head.index];
        A[newNode.index][nearEdge.head.index] =
                A[nearEdge.head.index][newNode.index] =
                        0.5 * (A[nearEdge.head.index][root.index]
                                + A[v.index][nearEdge.head.index]);
        sib = nearEdge.siblingEdge();
        if (null != sib) {
          updateSubTree(A, sib, v, root, newNode, 0.5 * dcoeff,
                  direction.SKEW);
        }
        if (null != nearEdge.tail.parentEdge) {
          updateSubTree(A, nearEdge.tail.parentEdge, v, root,
                  newNode, 0.5 * dcoeff, direction.DOWN);
        }
        updatePair(A, nearEdge, nearEdge, v, root, dcoeff, direction.DOWN);
        break;
      case SKEW: //newNode is neither above nor below nearEdge
        A[v.index][nearEdge.head.index] = A[nearEdge.head.index][v.index];
        A[newNode.index][nearEdge.head.index] =
                A[nearEdge.head.index][newNode.index] =
                        0.5 * (A[nearEdge.head.index][root.index] +
                                A[nearEdge.head.index][v.index]);
        if (null != nearEdge.head.leftEdge) {
          updateSubTree(A, nearEdge.head.leftEdge, v, root,
                  newNode, 0.5 * dcoeff, direction.SKEW);
        }
        if (null != nearEdge.head.rightEdge) {
          updateSubTree(A, nearEdge.head.rightEdge, v, root,
                  newNode, 0.5 * dcoeff, direction.SKEW);
        }
        updatePair(A, nearEdge, nearEdge, v, root, dcoeff, direction.UP);
    }
  }

  private void BMEupdateAveragesMatrix(double[][] A, edge e, node v,
                                       node newNode) {
    edge sib, par, left, right;
    //first, update the v,newNode entries
    A[newNode.index][newNode.index] = 0.5 * (A[e.head.index][e.head.index]
            + A[v.index][e.head.index]);
    A[v.index][newNode.index] = A[newNode.index][v.index] =
            A[v.index][e.head.index];
    A[v.index][v.index] =
            0.5 * (A[e.head.index][v.index] + A[v.index][e.head.index]);
    left = e.head.leftEdge;
    right = e.head.rightEdge;
    if (null != left) // updates left and below
    {
      updateSubTree(A, left, v, e.head, newNode, 0.25, direction.UP);
    }
    if (null != right) // updates right and below
    {
      updateSubTree(A, right, v, e.head, newNode, 0.25, direction.UP);
    }
    sib = e.siblingEdge();
    if (null != sib) // updates sib and below
    {
      updateSubTree(A, sib, v, e.head, newNode, 0.25, direction.SKEW);
    }
    par = e.tail.parentEdge;
    if (null != par) // updates par and above
    {
      updateSubTree(A, par, v, e.head, newNode, 0.25, direction.DOWN);
    }
    /*must change values A[e.head][*] last, as they are used to update
             the rest of the matrix*/
    A[newNode.index][e.head.index] = A[e.head.index][newNode.index]
            = A[e.head.index][e.head.index];
    A[v.index][e.head.index] = A[e.head.index][v.index];
    //updates e.head fields only
    updatePair(A, e, e, v, e.head, 0.5, direction.UP);
  }

  double wf2(double lambda, double D_AD, double D_BC, double D_AC, double D_BD,
             double D_AB, double D_CD) {
    double weight;
    weight = 0.5 * (lambda * (D_AC + D_BD) + (1 - lambda) * (D_AD + D_BC)
            + (D_AB + D_CD));
    return (weight);
  }

  /*A is tree below sibling, B is tree below edge, C is tree above edge*/
  private double wf3(double D_AB, double D_AC, double D_kB, double D_kC) {
    return (D_AC + D_kB - D_AB - D_kC);
  }

  private void BMEtestEdge(edge e, node v, double[][] A) {
    edge up, down;
    down = e.siblingEdge();
    up = e.tail.parentEdge;
    e.totalweight = wf3(A[e.head.index][down.head.index],
            A[down.head.index][e.tail.index],
            A[e.head.index][v.index],
            A[v.index][e.tail.index])
            + up.totalweight;
  }

  private void BMEsplitEdge(node v, edge e, double[][] A) {
    edge newPendantEdge;
    edge newInternalEdge;
    node newNode;
    String nodeLabel = "I" + (size + 1);
    String edgeLabel1 = "E" + size;
    String edgeLabel2 = "E" + (size + 1);
    //make the new node and edges
    newNode = new node(nodeLabel, null, size + 1);
    newPendantEdge = new edge(edgeLabel1, newNode, v, 0.0);
    newInternalEdge = new edge(edgeLabel2, newNode, e.head, 0.0);
    //update the matrix of average distances
    BMEupdateAveragesMatrix(A, e, v, newNode);
    //put them in the correct topology
    newNode.parentEdge = e;
    e.head.parentEdge = newInternalEdge;
    v.parentEdge = newPendantEdge;
    e.head = newNode;
    size += 2;
    if (e.tail.leftEdge == e)
    //actually this is totally arbitrary and probably unnecessary
    {
      newNode.leftEdge = newInternalEdge;
      newNode.rightEdge = newPendantEdge;
    } else {
      newNode.leftEdge = newInternalEdge;
      newNode.rightEdge = newPendantEdge;
    }
  }

  /**
   * the key function of the program addSpeices inserts the node
   * v to the tree T.  It uses testEdge to see what the relative
   * weight would be if v split a particular edge.  Once insertion
   * point is found, v is added to T, and A is updated.  Edge
   * weights are not assigned until entire tree is build
   */
  void BMEaddSpecies(node v, double[][] D, double[][] A) {
    edge e; //loop variable
    edge e_min; //points to best edge seen thus far
    double w_min = 0.0;   //used to keep track of tree weights

    //initialize variables as necessary

    //CASE 1: T is empty, v is the first node
    if (this.root == null) {
      this.root = v;
      //	note that we are rooting T arbitrarily at a leaf.
      // T.root is not the phylogenetic root
      v.index = 0;
      this.size = 1;
      return;
    }
    //CASE 2: T is a single-vertex tree
    if (1 == size) {
      v.index = 1;
      e = new edge("E1", this.root, v, 0.0);
      A[v.index][v.index] = D[v.index2][root.index2];
      root.leftEdge = v.parentEdge = e;
      size = 2;
      return;
    }
    //CASE 3: T has at least two nodes and an edge.  Insert new node
    //by breaking one of the edges
    v.index = size;
    BMEcalcNewvAverages(v, D, A);
    /*calcNewvAverages will update A for the row and column
            include the node v.  Will do so using pre-existing averages in T and
            information from A,D*/
    e_min = root.leftEdge;
    e = e_min.head.leftEdge;
    while (null != e) {
      BMEtestEdge(e, v, A);
      /*testEdge tests weight of tree if loop variable
             e is the edge split, places this value in the e.totalweight field */
      if (e.totalweight < w_min) {
        e_min = e;
        w_min = e.totalweight;
      }
      e = topFirstTraverse(e);
    }
    //e_min now points at the edge we want to split
    BMEsplitEdge(v, e_min, A);
  }

  /**
   * The key function of the program addSpeices inserts
   * the node v to the tree T.  It uses testEdge to see what the
   * weight would be if v split a particular edge.  Weights
   * are assigned by OLS formula
   *
   * @param v the species to add
   * @param D the D matrix
   * @param A the A matrix
   */
  void GMEaddSpecies(node v, double[][] D, double[][] A) {
    edge e; // loop variable
    edge e_min; //points to best edge seen thus far
    double w_min = 0.0;   //used to keep track of tree weights

    // initialize variables as necessary
    //CASE 1: T is empty, v is the first node
    //create a tree with v as only vertex, no edges
    if (this.root == null) {
      this.root = v;
      //	note that we are rooting T arbitrarily at a leaf.
      // T.root is not the phylogenetic root
      v.index = 0;
      this.size = 1;
      return;
    }
    // CASE 2: T is a single-vertex tree
    if (this.size == 1) {
      v.index = 1;
      e = new edge("E1", this.root, v, 0.0);
      e.topsize = 1;
      e.bottomsize = 1;
      A[v.index][v.index] = D[v.index2][this.root.index2];
      this.root.leftEdge = v.parentEdge = e;
      this.size = 2;
      return;
    }
    // CASE 3: T has at least two nodes and an edge.
    // Insert new node by breaking one of the edges
    v.index = this.size;
    GMEcalcNewvAverages(v, D, A);
    /*calcNewvAverges will assign values to all the edge averages of T which
            include the node v.  Will do so using pre-existing averages in T and
            information from A,D*/
    e_min = this.root.leftEdge;
    e = e_min.head.leftEdge;
    while (null != e) {
      testEdge(e, v, A);
      /*testEdge tests weight of tree if loop variable
             e is the edge split, places this weight in e.totalweight field */
      if (e.totalweight < w_min) {
        e_min = e;
        w_min = e.totalweight;
      }
      e = topFirstTraverse(e);
    }
    /*e_min now points at the edge we want to split*/
    GMEsplitEdge(v, e_min, A);
  }

  void assignAllSizeFields() {
    root.leftEdge.assignBottomsize();
    root.leftEdge.assignTopsize(size / 2 + 1);
  }

  void makeOLSAveragesTable(double[][] D, double[][] A) {
    edge e, f, g, h;
    edge exclude;
    e = f = null;
    e = depthFirstTraverse(e);
    while (null != e) {
      f = e;
      exclude = e.tail.parentEdge;
      /* we want to calculate A[e.head][f.head] for all edges
             except those edges which are ancestral to e.  For those
             edges, we will calculate A[e.head][f.head] to have a
             different meaning, later*/
      if (e.head.leaf()) {
        while (null != f) {
          if (exclude != f) {
            if (f.head.leaf()) {
              A[e.head.index][f.head.index] =
                      A[f.head.index][e.head.index] =
                              D[e.head.index2][f.head.index2];
            } else {
              g = f.head.leftEdge;
              h = f.head.rightEdge;
              A[e.head.index][f.head.index] =
                      A[f.head.index][e.head.index] =
                              (g.bottomsize * A[e.head.index][g.head.index]
                                      + h.bottomsize * A[e.head.index][h.head.index])
                                      / f.bottomsize;
            }
          } else //exclude == f
          {
            exclude = exclude.tail.parentEdge;
          }
          f = depthFirstTraverse(f);
        }
      } else
      //e.head is not a leaf, so we go recursively to
      //values calculated for the nodes below e
      {
        while (null != f) {
          if (exclude != f) {
            g = e.head.leftEdge;
            h = e.head.rightEdge;
            A[e.head.index][f.head.index] =
                    A[f.head.index][e.head.index] =
                            (g.bottomsize * A[f.head.index][g.head.index]
                                    + h.bottomsize * A[f.head.index][h.head.index])
                                    / e.bottomsize;
          } else {
            exclude = exclude.tail.parentEdge;
          }
          f = depthFirstTraverse(f);
        }
      }
      /*now we move to fill up the rest of the table: we want
             A[e.head.index][f.head.index] for those cases where e is an
             ancestor of f, or vice versa.  We'll do this by choosing e via a
             depth first-search, and the recursing for f up the path to the
             root*/
      f = e.tail.parentEdge;
      if (null != f) {
        fillTableUp(e, f, A, D);
      }
      e = depthFirstTraverse(e);
    }

    /*we are indexing this table by vertex indices, but really the
         underlying object is the edge set.  Thus, the array is one element
         too big in each direction, but we'll ignore the entries involving the root,
         and instead refer to each edge by the head of that edge.  The head of
         the root points to the edge ancestral to the rest of the tree, so
         we'll keep track of up distances by pointing to that head*/

    /*10/13/2001: collapsed three depth-first searches into 1*/
  }

  /**
   * fillTableUp fills all the entries in D associated with
   * e.head,f.head and those edges g.head above e.head
   */
  void fillTableUp(edge e, edge f, double[][] A, double[][] D) {
    edge g, h;
    if (root == f.tail) {
      if (e.head.leaf()) {
        A[e.head.index][f.head.index] =
                A[f.head.index][e.head.index] =
                        D[e.head.index2][f.tail.index2];
      } else {
        g = e.head.leftEdge;
        h = e.head.rightEdge;
        A[e.head.index][f.head.index] =
                A[f.head.index][e.head.index] =
                        (g.bottomsize * A[f.head.index][g.head.index]
                                + h.bottomsize * A[f.head.index][h.head.index])
                                / e.bottomsize;
      }
    } else {
      g = f.tail.parentEdge;
      fillTableUp(e, g, A, D); /*recursive call*/
      h = f.siblingEdge();
      A[e.head.index][f.head.index] =
              A[f.head.index][e.head.index] =
                      (g.topsize * A[e.head.index][g.head.index]
                              + h.bottomsize * A[e.head.index][h.head.index])
                              / f.topsize;
    }
  }

  int NNI(double[][] avgDistArray, int count) {
    edge e, centerEdge;
    edge[] edgeArray;
    direction[] location;
    heap p, q;
    int i;
    int possibleSwaps;
    double[] weights;
    p = new heap(size + 1);
    q = new heap(size + 1);
    edgeArray = new edge[size + 1];
    weights = new double[size + 1];
    location = new direction[size + 1];
    for (i = 0; i < size + 1; i++) {
      weights[i] = 0.0;
      location[i] = direction.NONE;
    }
    e = root.leftEdge.findBottomLeft();
    /* *count = 0; */
    while (null != e) {
      edgeArray[e.head.index + 1] = e;
      location[e.head.index + 1] =
              NNIEdgeTest(e, avgDistArray, weights, e.head.index + 1);
      e = depthFirstTraverse(e);
    }
    possibleSwaps = p.makeThreshHeap(q, weights, size + 1, 0.0);
    p.permInverse(q, size + 1);
    /*we put the negative values of weights into a heap, indexed by p
         with the minimum value pointed to by p[1]*/
    /*p[i] is index (in edgeArray) of edge with i-th position
         in the heap, q[j] is the position of edge j in the heap */
    while (weights[p.p[1]] < 0) {
      centerEdge = edgeArray[p.p[1]];
      count++;
      weight += weights[p.p[1]];
      NNItopSwitch(edgeArray[p.p[1]], location[p.p[1]], avgDistArray);
      location[p.p[1]] = direction.NONE;
      weights[p.p[1]] = 0.0;  /*after the NNI, this edge is in optimal
				      configuration*/
      p.popHeap(q, weights, possibleSwaps--, 1);
      /*but we must retest the other four edges*/
      e = centerEdge.head.leftEdge;
      possibleSwaps = NNIRetestEdge(p, q, e, avgDistArray, weights,
              location, possibleSwaps);
      e = centerEdge.head.rightEdge;
      possibleSwaps = NNIRetestEdge(p, q, e, avgDistArray, weights,
              location, possibleSwaps);
      e = centerEdge.siblingEdge();
      possibleSwaps = NNIRetestEdge(p, q, e, avgDistArray, weights,
              location, possibleSwaps);
      e = centerEdge.tail.parentEdge;
      possibleSwaps = NNIRetestEdge(p, q, e, avgDistArray, weights,
              location, possibleSwaps);
    }
    return count;
  }

  int NNIRetestEdge(heap p, heap q, edge e, double[][] avgDistArray,
                    double[] weights, direction[] location, int possibleSwaps) {
    direction tloc = location[e.head.index + 1];
    location[e.head.index + 1] =
            NNIEdgeTest(e, avgDistArray, weights, e.head.index + 1);
    if (direction.NONE == location[e.head.index + 1]) {
      if (direction.NONE != tloc) {
        p.popHeap(q, weights, possibleSwaps--, q.p[e.head.index + 1]);
      }
    } else {
      if (direction.NONE == tloc) {
        p.pushHeap(q, weights, possibleSwaps++, q.p[e.head.index + 1]);
      } else {
        p.reHeapElement(q, weights, possibleSwaps, q.p[e.head.index + 1]);
      }
    }
    return possibleSwaps;
  }

  direction NNIEdgeTest(edge e, double[][] A, double[] weight, int wIndex) {
    int a, b, c, d;
    edge f;
    double[] lambda;
    double D_LR, D_LU, D_LD, D_RD, D_RU, D_DU;
    double w1, w2, w0;

    if (e.tail.leaf() || e.head.leaf()) {
      return direction.NONE;
    }
    lambda = new double[3];
    ;
    a = e.tail.parentEdge.topsize;
    f = e.siblingEdge();
    b = f.bottomsize;
    c = e.head.leftEdge.bottomsize;
    d = e.head.rightEdge.bottomsize;

    lambda[0] = ((double) b * c + a * d) / ((a + b) * (c + d));
    lambda[1] = ((double) b * c + a * d) / ((a + c) * (b + d));
    lambda[2] = ((double) c * d + a * b) / ((a + d) * (b + c));

    D_LR = A[e.head.leftEdge.head.index][e.head.rightEdge.head.index];
    D_LU = A[e.head.leftEdge.head.index][e.tail.index];
    D_LD = A[e.head.leftEdge.head.index][f.head.index];
    D_RU = A[e.head.rightEdge.head.index][e.tail.index];
    D_RD = A[e.head.rightEdge.head.index][f.head.index];
    D_DU = A[e.tail.index][f.head.index];

    w0 = wf2(lambda[0], D_RU, D_LD, D_LU, D_RD, D_DU, D_LR);
    w1 = wf2(lambda[1], D_RU, D_LD, D_DU, D_LR, D_LU, D_RD);
    w2 = wf2(lambda[2], D_DU, D_LR, D_LU, D_RD, D_RU, D_LD);
    if (w0 <= w1) {
      if (w0 <= w2) //w0 <= w1,w2
      {
        weight[wIndex] = 0.0;
        return direction.NONE;
      } else //w2 < w0 <= w1
      {
        weight[wIndex] = w2 - w0;
        return direction.RIGHT;
      }
    } else if (w2 <= w1) //w2 <= w1 < w0
    {
      weight[wIndex] = w2 - w0;
      return direction.RIGHT;
    } else //w1 < w2, w0
    {
      weight[wIndex] = w1 - w0;
      return direction.LEFT;
    }
  }

  void NNItopSwitch(edge e, direction d, double[][] A) {
    edge par, fixed;
    edge skew, swap;

    if (direction.LEFT == d) {
      swap = e.head.leftEdge;
    } else {
      swap = e.head.rightEdge;
    }
    skew = e.siblingEdge();
    fixed = swap.siblingEdge();
    par = e.tail.parentEdge;
    /*perform topological switch by changing f from (u,b) to (v,b)
             and g from (v,c) to (u,c), necessitates also changing parent fields*/
    swap.tail = e.tail;
    skew.tail = e.head;

    if (direction.LEFT == d) {
      e.head.leftEdge = skew;
    } else {
      e.head.rightEdge = skew;
    }
    if (skew == e.tail.rightEdge) {
      e.tail.rightEdge = swap;
    } else {
      e.tail.leftEdge = swap;
    }
    //both topsize and bottomsize change for e, but nowhere else
    e.topsize = par.topsize + swap.bottomsize;
    e.bottomsize = fixed.bottomsize + skew.bottomsize;
    NNIupdateAverages(A, e, par, skew, swap, fixed);
  }

  void NNIupdateAverages(double[][] A, edge e, edge par, edge skew,
                         edge swap, edge fixed) {
    node v;
    edge elooper;
    v = e.head;
    //first, v
    A[e.head.index][e.head.index] =
            (swap.bottomsize *
                    ((skew.bottomsize * A[skew.head.index][swap.head.index]
                            + fixed.bottomsize * A[fixed.head.index][swap.head.index])
                            / e.bottomsize) +
                    par.topsize *
                            ((skew.bottomsize * A[skew.head.index][par.head.index]
                                    + fixed.bottomsize * A[fixed.head.index][par.head.index])
                                    / e.bottomsize)
            ) / e.topsize;
    // next, we loop over all the edges
    // which are below e
    elooper = e.findBottomLeft();
    while (e != elooper) {
      A[e.head.index][elooper.head.index] =
              A[elooper.head.index][v.index]
                      = (swap.bottomsize * A[elooper.head.index][swap.head.index] +
                      par.topsize * A[elooper.head.index][par.head.index])
                      / e.topsize;
      elooper = depthFirstTraverse(elooper);
    }
    // next we loop over all the edges below and
    // including swap
    elooper = swap.findBottomLeft();
    while (swap != elooper) {
      A[e.head.index][elooper.head.index] =
              A[elooper.head.index][e.head.index]
                      = (skew.bottomsize * A[elooper.head.index][skew.head.index] +
                      fixed.bottomsize * A[elooper.head.index][fixed.head.index])
                      / e.bottomsize;
      elooper = depthFirstTraverse(elooper);
    }
    // now elooper = skew
    A[e.head.index][elooper.head.index] =
            A[elooper.head.index][e.head.index]
                    = (skew.bottomsize * A[elooper.head.index][skew.head.index] +
                    fixed.bottomsize * A[elooper.head.index][fixed.head.index])
                    / e.bottomsize;

    // finally, we loop over all the edges in the tree
    // on the far side of parEdge
    elooper = root.leftEdge;
    while ((elooper != swap) && (elooper != e)) //start a top-first traversal
    {
      A[e.head.index][elooper.head.index] =
              A[elooper.head.index][e.head.index]
                      = (skew.bottomsize * A[elooper.head.index][skew.head.index]
                      + fixed.bottomsize * A[elooper.head.index][fixed.head.index])
                      / e.bottomsize;
      elooper = topFirstTraverse(elooper);
    }
    // At this point, elooper = par.
    // We finish the top-first traversal, excluding the subtree below par
    elooper = par.moveUpRight();
    while (null != elooper) {
      A[e.head.index][elooper.head.index]
              = A[elooper.head.index][e.head.index]
              = (skew.bottomsize * A[elooper.head.index][skew.head.index] +
              fixed.bottomsize * A[elooper.head.index][fixed.head.index])
              / e.bottomsize;
      elooper = topFirstTraverse(elooper);
    }
  }

  void assignOLSWeights(double[][] A) {
    edge e;
    e = depthFirstTraverse(null);
    while (null != e) {
      if (e.head.leaf() || e.tail.leaf()) {
        e.OLSext(A);
      } else {
        e.OLSint(A);
      }
      e = depthFirstTraverse(e);
    }
  }

  void makeBMEAveragesTable(double[][] D, double[][] A) {
    edge e, f, exclude;
    node u, v;
    // first, let's deal with the averages involving the root of T
    e = root.leftEdge;
    f = depthFirstTraverse(null);
    while (null != f) {
      if (f.head.leaf()) {
        A[e.head.index][f.head.index] = A[f.head.index][e.head.index]
                = D[e.tail.index2][f.head.index2];
      } else {
        u = f.head.leftEdge.head;
        v = f.head.rightEdge.head;
        A[e.head.index][f.head.index] = A[f.head.index][e.head.index]
                = 0.5 * (A[e.head.index][u.index] + A[e.head.index][v.index]);
      }
      f = depthFirstTraverse(f);
    }
    e = depthFirstTraverse(null);
    while (root.leftEdge != e) {
      f = exclude = e;
      while (root.leftEdge != f) {
        if (f == exclude) {
          exclude = exclude.tail.parentEdge;
        } else if (e.head.leaf()) {
          if (f.head.leaf()) {
            A[e.head.index][f.head.index] =
                    A[f.head.index][e.head.index]
                            = D[e.head.index2][f.head.index2];
          } else {
            //since f is chosen using a
            // depth-first search, other values
            // have been calculated
            u = f.head.leftEdge.head;
            v = f.head.rightEdge.head;
            A[e.head.index][f.head.index]
                    = A[f.head.index][e.head.index]
                    = 0.5 * (A[e.head.index][u.index]
                    + A[e.head.index][v.index]);
          }
        } else {
          u = e.head.leftEdge.head;
          v = e.head.rightEdge.head;
          A[e.head.index][f.head.index]
                  = A[f.head.index][e.head.index]
                  = 0.5 * (A[f.head.index][u.index]
                  + A[f.head.index][v.index]);
        }
        f = depthFirstTraverse(f);
      }
      e = depthFirstTraverse(e);
    }
    e = depthFirstTraverse(null);
    while (root.leftEdge != e) {
      // calculates averages for
      // A[e.head.index][g.head.index] for
      // any edge g in path from e to root of tree
      calcUpAverages(D, A, e, e);
      e = depthFirstTraverse(e);
    }
  }

  /**
   * calcUpAverages will ensure that A[e.head.index][f.head.index]
   * is filled for any f >= g.  Works recursively
   */
  void calcUpAverages(double[][] D, double[][] A, edge e, edge g) {
    node u, v;
    edge s;
    if (!g.tail.leaf()) {
      calcUpAverages(D, A, e, g.tail.parentEdge);
      s = g.siblingEdge();
      u = g.tail;
      v = s.head;
      A[e.head.index][g.head.index] = A[g.head.index][e.head.index]
              = 0.5 * (A[e.head.index][u.index] + A[e.head.index][v.index]);
    }
  }

  int bNNI(double[][] avgDistArray, int count) {
    edge e;
    edge[] edgeArray;
    heap p, q;
    direction[] location;
    int i;
    int possibleSwaps;
    double[] weights;
    //FILE *mfile;
    p = new heap(size + 1);
    q = new heap(size + 1);
    edgeArray = new edge[size + 1];
    weights = new double[size + 1];
    location = new direction[size + 1];
    for (i = 0; i < size + 1; i++) {
      weights[i] = 0.0;
      location[i] = direction.NONE;
    }
    e = root.leftEdge.findBottomLeft();
    while (null != e) {
      edgeArray[e.head.index + 1] = e;
      location[e.head.index + 1] =
              bNNIEdgeTest(e, avgDistArray, weights, e.head.index + 1);
      e = depthFirstTraverse(e);
    }
    possibleSwaps = p.makeThreshHeap(q, weights, size + 1, 0.0);
    p.permInverse(q, size + 1);
    // we put the negative values of weights into a heap, indexed by p
    // with the minimum value pointed to by p[1]
    // p[i] is index (in edgeArray) of edge with i-th position
    // in the heap, q[j] is the position of edge j in the heap
    while (weights[p.p[1]] < 0) {
      //centerEdge = edgeArray[p.p[1]];
      count++;
      bNNItopSwitch(edgeArray[p.p[1]], location[p.p[1]], avgDistArray);
      location[p.p[1]] = direction.NONE;
      //after the bNNI, this edge is in optimal configuration
      weights[p.p[1]] = 0.0;
      p.popHeap(q, weights, possibleSwaps--, 1);
      // but we must retest the other edges of T
      // CHANGE 2/28/2003 expanding retesting to _all_ edges of T
      e = depthFirstTraverse(null);
      while (null != e) {
        possibleSwaps = bNNIRetestEdge(p, q, e, avgDistArray, weights,
                location, possibleSwaps);
        e = depthFirstTraverse(e);
      }
    }
    assignBalWeights(avgDistArray);
    return count;
  }

  direction bNNIEdgeTest(edge e, double[][] A, double[] weight, int wPos) {
    edge f;
    double D_LR, D_LU, D_LD, D_RD, D_RU, D_DU;
    double w1, w2, w0;
    if (e.tail.leaf() || e.head.leaf()) {
      return direction.NONE;
    }
    f = e.siblingEdge();
    D_LR = A[e.head.leftEdge.head.index][e.head.rightEdge.head.index];
    D_LU = A[e.head.leftEdge.head.index][e.tail.index];
    D_LD = A[e.head.leftEdge.head.index][f.head.index];
    D_RU = A[e.head.rightEdge.head.index][e.tail.index];
    D_RD = A[e.head.rightEdge.head.index][f.head.index];
    D_DU = A[e.tail.index][f.head.index];

    w0 = wf5(D_RU, D_LD, D_LU, D_RD, D_DU, D_LR); //weight of current config
    w1 = wf5(D_RU, D_LD, D_DU, D_LR, D_LU, D_RD); //weight with L<.D switch
    w2 = wf5(D_DU, D_LR, D_LU, D_RD, D_RU, D_LD); //weight with R<.D switch
    if (w0 <= w1) {
      if (w0 <= w2) // w0 <= w1,w2
      {
        weight[wPos] = 0.0;
        return direction.NONE;
      } else // w2 < w0 <= w1
      {
        weight[wPos] = w2 - w0;
        return direction.RIGHT;
      }
    } else if (w2 <= w1) // w2 <= w1 < w0
    {
      weight[wPos] = w2 - w0;
      return direction.RIGHT;
    } else // w1 < w2, w0
    {
      weight[wPos] = w1 - w0;
      return direction.LEFT;
    }
  }

  void bNNItopSwitch(edge e, direction d, double[][] A) {
    edge down, swap, fixed;
    node u, v;
    down = e.siblingEdge();
    u = e.tail;
    v = e.head;
    if (d == direction.LEFT) {
      swap = e.head.leftEdge;
      fixed = e.head.rightEdge;
      v.leftEdge = down;
    } else {
      swap = e.head.rightEdge;
      fixed = e.head.leftEdge;
      v.rightEdge = down;
    }
    swap.tail = u;
    down.tail = v;
    if (e.tail.leftEdge == e) {
      u.rightEdge = swap;
    } else {
      u.leftEdge = swap;
    }
    bNNIupdateAverages(A, v, e.tail.parentEdge, down, swap, fixed);
  }

  int bNNIRetestEdge(heap p, heap q, edge e, double[][] avgDistArray,
                     double[] weights, direction[] location, int possibleSwaps) {
    direction tloc;
    tloc = location[e.head.index + 1];
    location[e.head.index + 1] =
            bNNIEdgeTest(e, avgDistArray, weights, e.head.index + 1);
    if (direction.NONE == location[e.head.index + 1]) {
      if (direction.NONE != tloc) {
        p.popHeap(q, weights, possibleSwaps--, q.p[e.head.index + 1]);
      }
    } else {
      if (direction.NONE == tloc) {
        p.pushHeap(q, weights, possibleSwaps++, q.p[e.head.index + 1]);
      } else {
        p.reHeapElement(q, weights, possibleSwaps, q.p[e.head.index + 1]);
      }
    }
    return possibleSwaps;
  }

  void assignBalWeights(double[][] A) {
    edge e;
    e = depthFirstTraverse(null);
    while (null != e) {
      if (e.head.leaf() || e.tail.leaf()) {
        e.WFext(A);
      } else {
        e.WFint(A);
      }
      e = depthFirstTraverse(e);
    }
  }

  void assignBMEWeights(double[][] A) {
    edge e;
    e = depthFirstTraverse(null);
    while (null != e) {
      if (e.head.leaf() || e.tail.leaf()) {
        e.BalWFext(A);
      } else {
        e.BalWFint(A);
      }
      e = depthFirstTraverse(e);
    }
  }

  /*swapping across edge whose head is v*/
  void bNNIupdateAverages(double[][] A, node v, edge par, edge skew,
                          edge swap, edge fixed) {
    A[v.index][v.index] = 0.25 * (A[fixed.head.index][par.head.index] +
            A[fixed.head.index][swap.head.index] +
            A[skew.head.index][par.head.index] +
            A[skew.head.index][swap.head.index]);
    updateSubTreeAfterNNI(A, v, fixed, skew.head, swap.head, 0.25, direction.UP);
    updateSubTreeAfterNNI(A, v, par, swap.head, skew.head, 0.25, direction.DOWN);
    updateSubTreeAfterNNI(A, v, skew, fixed.head, par.head, 0.25, direction.UP);
    updateSubTreeAfterNNI(A, v, swap, par.head, fixed.head, 0.25, direction.SKEW);
  }

  /**
   * This function is the meat of the average distance matrix
   * recalculation. Idea is: we are looking at the subtree rooted at
   * rootEdge. The subtree rooted at closer is closer to rootEdge after
   * the NNI, while the subtree rooted at further is further to rootEdge
   * after the NNI.  direction tells the direction of the NNI with respect
   * to rootEdge
   */
  void updateSubTreeAfterNNI(double[][] A, node v, edge rootEdge,
                             node closer, node further, double dcoeff, direction d) {
    edge sib;
    switch (d) {
      case UP: //rootEdge is below the center edge of the NNI
        //recursive calls to subtrees, if necessary
        if (null != rootEdge.head.leftEdge) {
          updateSubTreeAfterNNI(A, v, rootEdge.head.leftEdge,
                  closer, further, 0.5 * dcoeff, direction.UP);
        }
        if (null != rootEdge.head.rightEdge) {
          updateSubTreeAfterNNI(A, v, rootEdge.head.rightEdge,
                  closer, further, 0.5 * dcoeff, direction.UP);
        }
        updatePair(A, rootEdge, rootEdge, closer, further, dcoeff,
                direction.UP);
        sib = v.parentEdge.siblingEdge();
        A[rootEdge.head.index][v.index] =
                A[v.index][rootEdge.head.index] =
                        0.5 * A[rootEdge.head.index][sib.head.index] +
                                0.5 * A[rootEdge.head.index][v.parentEdge.tail.index];
        break;
      case DOWN: // rootEdge is above the center edge of the NNI
        sib = rootEdge.siblingEdge();
        if (null != sib) {
          updateSubTreeAfterNNI(A, v, sib, closer, further,
                  0.5 * dcoeff, direction.SKEW);
        }
        if (null != rootEdge.tail.parentEdge) {
          updateSubTreeAfterNNI(A, v, rootEdge.tail.parentEdge,
                  closer, further, 0.5 * dcoeff, direction.DOWN);
        }
        updatePair(A, rootEdge, rootEdge, closer, further, dcoeff,
                direction.DOWN);
        A[rootEdge.head.index][v.index] =
                A[v.index][rootEdge.head.index] =
                        0.5 * A[rootEdge.head.index][v.leftEdge.head.index] +
                                0.5 * A[rootEdge.head.index][v.rightEdge.head.index];
        break;
      case SKEW: // rootEdge is in subtree skew to v
        if (null != rootEdge.head.leftEdge) {
          updateSubTreeAfterNNI(A, v, rootEdge.head.leftEdge,
                  closer, further, 0.5 * dcoeff, direction.SKEW);
        }
        if (null != rootEdge.head.rightEdge) {
          updateSubTreeAfterNNI(A, v, rootEdge.head.rightEdge,
                  closer, further, 0.5 * dcoeff, direction.SKEW);
        }
        updatePair(A, rootEdge, rootEdge, closer, further, dcoeff,
                direction.UP);
        A[rootEdge.head.index][v.index] =
                A[v.index][rootEdge.head.index] =
                        0.5 * A[rootEdge.head.index][v.leftEdge.head.index] +
                                0.5 * A[rootEdge.head.index][v.rightEdge.head.index];
        break;
    }
  }

  double wf5(double D_AD, double D_BC, double D_AC, double D_BD,
             double D_AB, double D_CD) {
    double weight;
    weight = 0.25 * (D_AC + D_BD + D_AD + D_BC) + 0.5 * (D_AB + D_CD);
    return (weight);
  }

  /**
   * Print out the tree in Newick format to std out
   *
   * @param ofile name of the output file
   */
  public void NewickPrintTree(PrintStream out) throws Exception {
    if (root.leaf()) {
      NewickPrintBinaryTree(out);
    } else {
      NewickPrintTrinaryTree(out);
    }

  }

  void NewickPrintBinaryTree(PrintStream out) throws Exception {
    NumberFormat nf = NumberFormat.getInstance();
    nf.setMaximumFractionDigits(DOUBLE_PRECISION);
    edge e, f;
    node rootchild;
    e = root.leftEdge;
    rootchild = e.head;
    out.write('(');
    f = rootchild.leftEdge;
    if (null != f) {
      NewickPrintSubtree(f, out);
      out.write(',');
    }
    f = rootchild.rightEdge;
    if (null != f) {
      NewickPrintSubtree(f, out);
      out.write(',');
    }
    out.write(root.label.getBytes());
    out.write(':');
    out.write(nf.format(e.distance).getBytes());
    out.write(')');
    if (null != rootchild.label) {
      out.write(rootchild.label.getBytes());
    }
    out.write(';');
    out.write('\n');
  }

  void NewickPrintTrinaryTree(PrintStream out) throws Exception {
    edge f;
    f = root.leftEdge;
    out.write('(');
    if (null != f) {
      NewickPrintSubtree(f, out);
      out.write(',');
    }
    f = root.rightEdge;
    if (null != f) {
      NewickPrintSubtree(f, out);
      out.write(',');
    }
    f = root.middleEdge;
    if (null != f) {
      NewickPrintSubtree(f, out);
      out.write(')');
    }
    if (null != root.label) {
      out.write(root.label.getBytes());
    }
    out.write(';');
    out.write('\n');
  }

  void NewickPrintSubtree(edge e, PrintStream out) throws Exception {
    NumberFormat nf = NumberFormat.getInstance();
    nf.setMaximumFractionDigits(DOUBLE_PRECISION);
    if (null == e) {
      throw new Exception("Error with Newick Printing routine");
    }
    if (!e.head.leaf()) {
      out.write('(');
      NewickPrintSubtree(e.head.leftEdge, out);
      out.write(',');
      NewickPrintSubtree(e.head.rightEdge, out);
      out.write(')');
    }
    out.write(e.head.label.getBytes());
    out.write(':');
    out.write(nf.format(e.distance).getBytes());
  }
}
