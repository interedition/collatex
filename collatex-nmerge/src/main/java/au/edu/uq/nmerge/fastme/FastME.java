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

import java.util.StringTokenizer;


/**
 * Test program to generate a BME tree from a phylip format
 * difference matrix. Adapted from Desper and Gascuel's original
 * C program (2002). The advantage of this method is that a) no
 * negative branch lengths result (unlike Neighbour Join) and
 * b) it's fast.
 * See http://www.ncbi.nlm.nih.gov/CBBresearch/Desper/FastME.html
 *
 * @author Desmond Schmidt 24/12/10
 */
public class FastME {
  double[][] D;
  double[][] A;
  int numSpecies;
  set species;
  balance btype, ntype, wtype;
  public tree T;

  public FastME() {
    btype = balance.OLS;
    wtype = balance.OLS;
    ntype = balance.BAL;
  }

  /**
   * Set the type of balancing ot use
   *
   * @param btype either OLS or BAL
   */
  void setBalance(balance btype) {
    this.btype = btype;
  }

  /**
   * Parse a single row of diff data
   *
   * @param index the index of the rotree w to parse
   * @param size  the size of the row
   * @param row   the row data as a string
   */
  void parseRow(int index, int size, String row) {
    StringTokenizer st = new StringTokenizer(row, " ");
    node v = new node(st.nextToken(), null, -1);
    v.index2 = index;
    if (species == null)
      species = new set(v);
    else
      species.addToSet(v);
    for (int j = 0; j < size; j++) {
      String token = st.nextToken();
      D[index][j] = Double.parseDouble(token);
    }
  }

  /**
   * Create the A matrix
   *
   * @param d the size of the A matrix
   */
  void initDoubleMatrix(int d) {
    A = new double[d][d];
    for (int i = 0; i < d; i++) {
      for (int j = 0; j < d; j++)
        A[i][j] = 0.0;
    }
  }

  /**
   * Actually build the entire tree, one node at a time
   *
   * @param matrix an NxN difference matrix computed from the MVD
   * @param taxa   the short names of the versions
   */
  public void buildTree(double[][] matrix, String[] taxa) {
    set slooper;
    numSpecies = matrix.length;
    D = matrix;
    for (int i = 0; i < taxa.length; i++) {
      node v = new node(taxa[i], null, -1);
      v.index2 = i;
      if (species == null)
        species = new set(v);
      else
        species.addToSet(v);
    }
    initDoubleMatrix(2 * numSpecies - 2);
    T = new tree();
    switch (btype) {
      case OLS:
        for (slooper = species; slooper != null; slooper = slooper.next)
          T.GMEaddSpecies(slooper.node, D, A);
        break;
      case BAL:
        for (slooper = species; slooper != null; slooper = slooper.next)
          T.BMEaddSpecies(slooper.node, D, A);
        break;
    }
  }

  /**
   * I think this routine refines the rough tree produced by
   * buildTree
   */
  public void refineTree() throws Exception {
    int nniCount = 0;
    switch (ntype) {
      case OLS:
        if (btype != balance.OLS)
          T.assignAllSizeFields();
        T.makeOLSAveragesTable(D, A);
        nniCount = T.NNI(A, nniCount);
        T.assignOLSWeights(A);
        break;
      case BAL:
        if (btype != balance.BAL)
          T.makeBMEAveragesTable(D, A);
        nniCount = T.bNNI(A, nniCount);
        T.assignBMEWeights(A);
        break;
      case NONE:
        switch (wtype) {
          case OLS:    // default
            if (balance.OLS != btype)
              T.assignAllSizeFields();
            T.makeOLSAveragesTable(D, A);
            T.assignOLSWeights(A);
            break;
          case BAL:
            if (balance.BAL != btype)
              T.makeBMEAveragesTable(D, A);
            T.assignBMEWeights(A);
            break;
          default:
            throw new Exception(
                    "Error in program: variable 'btype' has illegal value "
                            + btype);
        }
        break;
      default:
        throw new Exception(
                "Error in program: variable 'ntype' has illegal value "
                        + ntype);
    }
  }
}
