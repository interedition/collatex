package eu.interedition.collatex.dekker.vectorspace;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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

/*
 * @author: Ronald Haentjens Dekker
 * 
 * Steps: 
 * 1. Tokenize, normalize the witnesses
 * 2. Do the matching
 *    Match every witness with every other witness
 *    Match every token from one witness with every token from
 *    the other witness.
 * 3. Build the vector space from the matches
 * 4. Find the optimal alignment in the vector space
 *    based on the length of the vectors and possible
 *    conflicts between dimensions of vectors.
 * 5. Build the variant graph from the optimal vectors.  
 * 
 * NOTE: steps 2 and 3 are logically separate steps, but the
 * implementation performs them together, so that the matches
 * do not have to be stored.
 * 
 */
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

  public void collate(VariantGraph graph, SimpleWitness a, SimpleWitness b, SimpleWitness c) {
    // Step 1: do the matching and fill the vector space
    // first compare witness 1 and 2
    // then compare 1 and 3
    // then 2 and 3
    compareWitnesses(a, b, 0, 1);
    compareWitnesses(a, c, 0, 2);
    compareWitnesses(b, c, 1, 2);
    // Step 2: optimize the alignment...
    optimizeAlignment();
    // Step 3: build the variant graph from the vector space
    // merge the first witness in
    // there are no alignments
    merge(graph, a, Collections.<Token, VariantGraph.Vertex> emptyMap());
    // convert vectors to <Token, Token> (Witness, other)
    Map<Token, Token> alignments = Maps.newHashMap();
    for (VectorSpace.Vector v : s.getVectors()) {
      //check whether this vector is present in both dimensions
      if (!(v.isPresentIn(0)&&v.isPresentIn(1))) {
        continue;
      }
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

  public void collate(VariantGraph graph, SimpleWitness a, SimpleWitness b) {
    SimpleWitness c = new SimpleWitness("c");
    collate(graph, a, b, c);
  }

  public List<Vector> getAlignment() {
    return s.getVectors();
  }

  /*
   * This method find the optimal alignment by reducing the number of vectors in
   * the vector space.
   */
  private void optimizeAlignment() {
    if (s.getVectors().isEmpty()) {
      throw new RuntimeException("Vector space is empty! There is nothing to align!");
    }
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
    for (int vectorLength = max; vectorLength > 0; vectorLength--) {
      LOG.fine("Checking vectors of size: " + vectorLength);
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
   * For all the possible vectors of a certain length this method checks whether
   * they conflict with one of the previously committed vectors. If so, the
   * possible vector is removed from the map. TODO: Or in case of overlap, split
   * into a smaller vector and then put in back into the map Note that this
   * method changes the possible vectors map.
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

  /*
   * Do the matching between tokens of two witness and add vectors for the
   * matches.
   */
  private void compareWitnesses(SimpleWitness a, SimpleWitness b, int dimensionA, int dimensionB) {
    //System.out.println("Comparing witness "+a.getSigil()+" and "+b.getSigil());
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
          s.addVector(coordinates);
        }
      }
    }
  }
  
  // dimension 0 = x
  // dimension 1 = y
  protected List<Token> getTokensFromVector(Vector v, int dimension, Iterable<Token> a) {
    int start = v.startCoordinate[dimension];
    if (start==0) {
      throw new RuntimeException("Vector "+v+" does not exist in dimension "+dimension);
    }
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
}
