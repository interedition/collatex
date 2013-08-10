package eu.interedition.collatex.dekker.vectorspace;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.vectorspace.VectorSpace.Vector;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.simple.SimpleWitness;

public class DekkerVectorSpaceAlgorithm extends CollationAlgorithm.Base {
  private VectorSpace s;

  public DekkerVectorSpaceAlgorithm() {
    this(new VectorSpace());
  }

  // for testing purposes
  protected DekkerVectorSpaceAlgorithm(VectorSpace s) {
    this.s = s;
  }

  @Override
  public void collate(VariantGraph against, Iterable<Token> witness) {
    throw new RuntimeException("Not yet implemented!");
  }

  @Override
  public void collate(VariantGraph against, Iterable<Token>... witnesses) {
    throw new RuntimeException("Not yet implemented!");
  }

  @Override
  public void collate(VariantGraph against, List<? extends Iterable<Token>> witnesses) {
    throw new RuntimeException("Not yet implemented!");
  }

  public void collate(VariantGraph graph, SimpleWitness a, SimpleWitness b) {
    // Step 1: do the matching and fill the vector space
    s.fill(a, b, new EqualityTokenComparator());

    // Step 2: optimize the alignment...
    // TODO: skipped for now

    // Step 3: build the variant graph from the vector space
    // merge the first witness in
    // there are no alignments
    merge(graph, a, Collections.<Token, VariantGraph.Vertex> emptyMap());

    // convert vectors to <Token, Token> (Witness, other)
    Map<Token, Token> alignments = Maps.newHashMap();
    for (VectorSpace.Vector v : s.getVectors()) {
      List<Token> tokensDimension1 = getTokensFromVector(v, 0, a);
      List<Token> tokensDimension2 = getTokensFromVector(v, 1, b);
      for (int i = 0; i < v.length; i++) {
        Token witnessTokenToAdd = tokensDimension2.get(i);
        Token tokenAlreadyInGraph = tokensDimension1.get(i);
        alignments.put(witnessTokenToAdd, tokenAlreadyInGraph);
      }
    }

    // now construct vertices/edges for witness b
    mergeTokens(graph, b, alignments);
  }

  // dimension 0 = x
  // dimension 1 = y
  protected List<Token> getTokensFromVector(Vector v, int dimension, Iterable<Token> a) {
    int start = v.startCoordinate[dimension];
    // iterate over the witness until the start position is reached.
    Iterator<Token> it = a.iterator();
    for (int i = 1; i < start; i++) {
      it.next();
    }
    // fetch the tokens in the range of the vector from the witness
    List<Token> tokens = Lists.newArrayListWithCapacity(v.length);
    for (int i = 1; i <= v.length; i++) {
      Token t = it.next();
      tokens.add(t);
    }
    return tokens;
  }

  public List<Vector> getAlignment() {
    optimizeAlignment();
    return s.getVectors();
  }

  /*
   * This method find the optimal alignment by reducing the number of
   * vectors in the vector space. 
   */
  private void optimizeAlignment() {
    // group the vectors together by length; vectors may change after commit 
    final Multimap<Integer, Vector> vectorMultimap;
    // sort the vectors based on length
    vectorMultimap = ArrayListMultimap.create();
    for (Vector v : s.getVectors()) {
      vectorMultimap.put(v.length, v);
    }
    // find the maximum vector size 
    Integer max = Collections.max(vectorMultimap.keySet());
    
    // traverse groups in descending order
    List<Vector> fixedVectors = Lists.newArrayList();
    for (int vectorLength=max; vectorLength > 0; vectorLength--) {
      LOG.fine("Checking vectors of size: "+vectorLength);
      // check the possible vectors of a certain length against 
      // the already committed vectors.
      removeImpossibleVectors(vectorLength, vectorMultimap, fixedVectors);
      // commit possible vectors
      List<Vector> possibleVectors = Lists.newArrayList(vectorMultimap.get(vectorLength));
      for (Vector v : possibleVectors) {
        fixedVectors.add(v);
      }
    }  
  }

  /*
   * For all the possible vectors of a certain length
   * this method checks whether they conflict with one of the
   * previously committed vectors.
   * If so, the possible vector is removed from the map.
   * TODO:
   * Or in case of overlap, split into a smaller vector
   * and then put in back into the map
   * Note that this method changes the possible vectors map.
   */
  private void removeImpossibleVectors(int islandSize, Multimap<Integer, Vector> vectorMultimap, List<Vector> fixedVectors) {
    Collection<Vector> vectorsToCheck = Lists.newArrayList(vectorMultimap.get(islandSize));
    for (Vector v : vectorsToCheck) {
      for (Vector f : fixedVectors) {
        if (f.conflictsWith(v)) {
          vectorMultimap.remove(islandSize, v);
          s.remove(v);
        }
      }
    }
  }
}
