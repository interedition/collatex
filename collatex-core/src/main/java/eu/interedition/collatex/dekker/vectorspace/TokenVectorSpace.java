package eu.interedition.collatex.dekker.vectorspace;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.matching.EqualityTokenComparator;

/*
 * @author: Ronald Haentjens Dekker
 * 
 * Vectors should have the highest dimensionality
 * first, the greatest length second.
 */
public class TokenVectorSpace extends VectorSpace {

  // do the matching and fill the vector space
  // first compare witness 1 and 2
  // then compare 1 and 3
  // then 2 and 3
  public void addWitnesses(Iterable<Token> a, Iterable<Token> b, Iterable<Token> c) {
    compareWitnesses(a, b, 0, 1);
    compareWitnesses(a, c, 0, 2);
    compareWitnesses(b, c, 1, 2);
    mergeAdjacentVectors();
  }

  void mergeAdjacentVectors() {
    List<Vector> mergedVectors = Lists.newArrayList();
    for (Vector v : vectors) {
      Vector adjacent = findAdjacentVector(mergedVectors, v);
      if (adjacent==null) {
        mergedVectors.add(v);
      } else {
        adjacent.extendLength();
      }
    }
    vectors.clear();
    vectors.addAll(mergedVectors);
  }

  private Vector findAdjacentVector(List<Vector> mergedVectors, Vector v) {
    for (Vector v2 : mergedVectors) {
      if (v2.isAdjacent(v)) {
        return v2;
      }
    }
    return null;
  }

  /*
   * Do the matching between tokens of two witness and add vectors for the
   * matches.
   */
  private void compareWitnesses(Iterable<Token> a, Iterable<Token> b, int dimensionA, int dimensionB) {
    // System.out.println("Comparing witness "+a.getSigil()+" and "+b.getSigil());
    Comparator<Token> comparator = new EqualityTokenComparator();
    int yCounter = 0;
    for (Token bToken : b) {
      yCounter++;
      int xCounter = 0;
      for (Token aToken : a) {
        xCounter++;
        if (comparator.compare(aToken, bToken) == 0) {
          int[] coordinates = new int[3];
          coordinates[dimensionA] = xCounter;
          coordinates[dimensionB] = yCounter;
          addVector(coordinates);
        }
      }
    }
  }

  @Override
  public Vector addVector(int... coordinates) {
    Vector vector = new Vector(1, coordinates[0], coordinates[1], coordinates[2]);
    Vector parallel = findParallelVector(vector);
    if (parallel != null) {
      vector = vector.merge(parallel);
      remove(parallel);
    }
    return super.add(vector);
  }

  public List<Vector> getAllVectorsContainingDimensionButNotAsMinimum(int dimension) {
    List<Vector> result = Lists.newArrayList();
    for (Vector v : vectors) {
      if (v.isPresentIn(dimension)) {
        Integer min = Collections.min(v.getDimensions());
        if (min != dimension) {
          result.add(v);
        }
      }
    }
    return result;
  }
}
