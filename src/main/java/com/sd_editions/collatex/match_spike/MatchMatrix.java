package com.sd_editions.collatex.match_spike;

import java.util.List;

import com.google.common.collect.Lists;

public class MatchMatrix {
  WordMatches[][] matrix;
  private final int witnesses;

  public MatchMatrix() {
    witnesses = 1;
    int c = 3;
    matrix = new WordMatches[witnesses][c];
    //    DenseMatrix matrix = new DenseMatrix(words, lines);
    // MatchMatrix is a matrix, where each row corresponds with a witness,
    // and each colum corresponds with a word in a witness.
    // each cell contains a word id top indicate which words match
  }

  public List<MatchMatrix> getPermutations() {
    // go through the matrix

    List<MatchMatrix> list = Lists.newArrayList();
    for (int row = 0; row < witnesses; row++) {
      for (int col = 0; col < matrix.length; col++) {
        WordMatches cell = matrix[row][col];
        boolean cellIsMultiple = multipleMatchesInWitness(cell);
        if (cellIsMultiple) {
          try {
            MatchMatrix copy = (MatchMatrix) this.clone();
            //            WordMatches cloneCell = (WordMatches) cell.clone();
            //            copy.setCell(row,col, cloneCell)
          } catch (CloneNotSupportedException e) {
            e.printStackTrace();
          }
        }

      }
    }
    return list;
  }

  // MatchVector: contains as many elements as there are witnesses
  // matchvector[i] = the place of the matching word in witness i

  private boolean multipleMatchesInWitness(WordMatches cell) {
    //    cell.getMatchOnWitsness(i);
    return false;
  }

  // Matrix of possible matches:
  // matrix[r][c] contains the list of possible matches for witness[r], word[c] in witnesses r+1..max

  //  a fine mess
  //  a fine day
  //  a bloody mess
  //  
  //  a[(2,1),(3,1)] fine[(2,2)] mess[(3,3)]
  //  a[(3,1)]       fine[]      day[]
  //  a[]       bloody[]    mess[]
  //
  //  to permutate: go from cell to cell
  //         if there's more than one possible match in a witness, split the matrix on these matches
}
