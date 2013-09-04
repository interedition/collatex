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
    List<Vector> newV = compareWitnesses(a, b, 0, 1);
    addVectors(newV);
    List<Vector> newV2 = compareWitnesses(a, c, 0, 2);
    mergeParallelVectors(newV2);
    List<Vector> newV3 = compareWitnesses(b, c, 1, 2);
    mergeParallelVectors(newV3);
    mergeAdjacentVectors();
  }

  private void addVectors(List<Vector> newV) {
    vectors.addAll(newV);
  }

  private void mergeParallelVectors(List<Vector> newVectors) {
    List<Vector> result = Lists.newArrayList();
    List<Vector> tobeRemoved = Lists.newArrayList();
    for (Vector nv : newVectors) {
      List<Vector> parallelV = findParallelVectors(nv);
      if (parallelV.isEmpty()) {
        result.add(nv);
      } else {
        for (Vector p : parallelV) {
          Vector m = p.merge(nv);
          tobeRemoved.add(p);
          result.add(m);
        }
      }
    }
    vectors.removeAll(tobeRemoved);
    vectors.addAll(result);
  }

  private List<Vector> findParallelVectors(Vector nv) {
    List<Vector> result = Lists.newArrayList();
    for (Vector v : vectors) {
      if (nv.isParallel(v)) {
        result.add(v);
      }
    }
    return result;
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
  private List<Vector> compareWitnesses(Iterable<Token> a, Iterable<Token> b, int dimensionA, int dimensionB) {
    //System.out.println("Comparing dimension: "+a + " and "+b);
    Comparator<Token> comparator = new EqualityTokenComparator();
    int yCounter = 0;
    List<Vector> result = Lists.newArrayList();
    for (Token bToken : b) {
      yCounter++;
      int xCounter = 0;
      for (Token aToken : a) {
        xCounter++;
        if (comparator.compare(aToken, bToken) == 0) {
          int[] coordinates = new int[3];
          coordinates[dimensionA] = xCounter;
          coordinates[dimensionB] = yCounter;
          result.add(new Vector(coordinates));
        }
      }
    }
    return result;
  }

  @Override
  public Vector addVector(int... coordinates) {
    Vector vector = new Vector(1, coordinates[0], coordinates[1], coordinates[2]);
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
