package eu.interedition.collatex.dekker.vectorspace;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.collect.Sets;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.VariantGraph.Vertex;
import eu.interedition.collatex.dekker.Match;
import eu.interedition.collatex.dekker.vectorspace.VectorSpace.Vector;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.simple.SimpleWitness;
import eu.interedition.collatex.util.VariantGraphRanking;

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
  private Map<Token, VariantGraph.Vertex> tokenToVertexMap;
  
  public DekkerVectorSpaceAlgorithm() {
    this(new VectorSpace());
    this.tokenToVertexMap = Maps.newHashMap();
  }

  // for testing purposes
  protected DekkerVectorSpaceAlgorithm(VectorSpace s) {
    this.s = s;
    this.tokenToVertexMap = Maps.newHashMap();
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
    SimpleWitness a = (SimpleWitness) witnesses.get(0);
    SimpleWitness b = (SimpleWitness) witnesses.get(1);
    SimpleWitness c;
    if (witnesses.size()>2) {
      c = (SimpleWitness) witnesses.get(2);
    } else {
      c = new SimpleWitness("c");
    }
    collate(against, a, b, c);
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
    //createVariantGraph(graph, a, b, c);
    build(graph, a, b, 0, 1);
  }

  private void createVariantGraph(VariantGraph graph, SimpleWitness a, SimpleWitness b, SimpleWitness c) {
    if (!graph.witnesses().isEmpty()) {
      throw new RuntimeException("Graph is not empty! This algorithm is not a progressive algorithm.");
    }
    // merge the first witness in
    // there are no alignments
    tokenToVertexMap.putAll(mergeTokens(graph, a, Collections.<Token, Vertex> emptyMap()));
    addCollationResultForWitnessPairToGraph(graph, a, b, 0, 1);
    // LOG.info("Merging the result from a and c");
    addCollationResultForWitnessPairToGraph(graph, a, c, 0, 2);
    // TODO: add b, c (this is more tricky)
  }

  private void addCollationResultForWitnessPairToGraph(VariantGraph graph, SimpleWitness a, SimpleWitness b, int dimensionA, final int dimensionB) {
    if (!b.iterator().hasNext()) {
      return;
    }
    Map<Token, Token> alignments = fillTokenTokenAlignmentsMap(a, b, dimensionA, dimensionB);

    List<Vector> vs = findAllVectorForADimension(dimensionA, dimensionB);
    // we order the vectors by their coordinate in dimension B
    Collections.sort(vs, new Comparator<Vector>(){
      @Override
      public int compare(Vector v1, Vector v2) {
        return v1.startCoordinate[dimensionB] - v2.startCoordinate[dimensionB];
      }});
    // now we have to check the order of the vectors to add
    // with the order in the variant graph
    // for this purpose we look at the ranking of the graph
    // for the first vertices of each of the vectors.
    List<List<Match>> phrases = Lists.newArrayList();
    Set<VariantGraph.Vertex> firstVertices = Sets.newHashSet();
    for (Vector v: vs) {
      List<Match> phrase = Lists.newArrayList();
      for (int i=0; i< v.length; i++) {
        Token tokenGraph = getTokensFromVector(v, dimensionA, a).get(i);
        Token tokenWitness = getTokensFromVector(v, dimensionB, b).get(i);
        VariantGraph.Vertex vx = tokenToVertexMap.get(tokenGraph);
        phrase.add(new Match(vx, tokenWitness));
      }
      phrases.add(phrase);
      firstVertices.add(phrase.get(0).vertex);
    }
    Set<Witness> witnesses = Sets.newHashSet();
    witnesses.add(a);
    witnesses.add(b);
    // rank vertices
    VariantGraphRanking ranking = VariantGraphRanking.ofOnlyCertainVertices(graph, witnesses, firstVertices);
    // gather matched ranks into a list ordered by their natural order
    final List<Integer> phraseRanks = Lists.newArrayList();
    for (List<Match> phrase : phrases) {
      phraseRanks.add(Preconditions.checkNotNull(ranking.apply(phrase.get(0).vertex)));
    }
    Collections.sort(phraseRanks);
    // detect transpositions
    final Stack<List<Match>> transpositions = new Stack<List<Match>>();
    int previousRank = 0;
    for (List<Match> phrase: phrases) {
      int rank = ranking.apply(phrase.get(0).vertex);
      int expectedRank = phraseRanks.get(previousRank);
      if (expectedRank != rank) { 
        addNewTransposition(phrase, transpositions);
      }
      previousRank++;
    }
    // now construct vertices/edges for witness b
    Map<Token, VariantGraph.Vertex> al = Maps.newHashMap();
    for (Entry<Token, Token> entry : alignments.entrySet()) {
      Vertex vertex = tokenToVertexMap.get(entry.getValue());
      al.put(entry.getKey(), vertex);
    }
    // remove transposed vertices from the alignment map
    for (List<Match> t : transpositions) {
      for (Match m : t) {
        al.remove(m.token);
      }
    }
    tokenToVertexMap.putAll(mergeTokens(graph, b, al));
    mergeTranspositions(graph, transpositions);
  }

  private void addNewTransposition(List<Match> phrase, Stack<List<Match>> transpositions) {
    LOG.fine("Transposition found! "+phrase);
    transpositions.add(phrase);
  }

  // convert vectors to <Token, Token> (Witness, other)
  private Map<Token, Token> fillTokenTokenAlignmentsMap(SimpleWitness a, SimpleWitness b, int dimensionA, int dimensionB) {
    Map<Token, Token> alignments = Maps.newHashMap();
    for (VectorSpace.Vector v : s.getVectors()) {
      //check whether this vector is present in both dimensions
      if (!(v.isPresentIn(dimensionA)&&v.isPresentIn(dimensionB))) {
        continue;
      }
      List<Token> tokensDimension1 = getTokensFromVector(v, dimensionA, a);
      List<Token> tokensDimension2 = getTokensFromVector(v, dimensionB, b);
      for (int i = 0; i < v.length; i++) {
        Token witnessTokenToAdd = tokensDimension2.get(i);
        Token tokenAlreadyInGraph = tokensDimension1.get(i);
        alignments.put(witnessTokenToAdd, tokenAlreadyInGraph);
      }
    }
    return alignments;
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
      // check for conflicts between the possible vectors
      checkConflicts(possibleVectors, vectorMultimap);
      for (Vector v : possibleVectors) {
        fixedVectors.add(v);
      }
    }
  }

  // check for partially overlap
  // the element that has the most conflicts should be removed
  private void checkConflicts(List<Vector> possibleVectors, Multimap<Integer, Vector> vectorMultimap) {
    Multiset<Vector> conflicts = HashMultiset.create();
    // TODO: this can be performed with less checks
    for (Vector v: possibleVectors) {
      for (Vector o: possibleVectors) {
        if (v==o) {
          continue;
        }
        if (v.overlapsPartially(o)) {
          LOG.info("Vector "+v+" partially overlaps with "+o);
          conflicts.add(v);
        }
      }
    }
    if (conflicts.isEmpty()) {
      return;
    }
    Vector cause = Multisets.copyHighestCountFirst(conflicts).iterator().next();
    // hack
    cause.length--;
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
          LOG.fine(String.format("%s conflicts with %s", f, v));
          vectorMultimap.remove(islandSize, v);
          s.remove(v);
          break;
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
  
  public void build(VariantGraph graph, Iterable<Token> a, Iterable<Token> b, int dimensionA, int dimensionB) {
    /*
     *  take the vectors from the vectorspace.
     *  they represent the alignment
     *  build a Vector -> List<vertex> representation
     *  1)Building the graph for the first witness is
     *  1) easy --> a vertex for every token
     *  2)then build the initial vector to vertex map
     *  find all the vector that have a coordinate in the 
     *  first dimension (that is the dimension related to
     *  the first witness)  
     *  3) Merge in witness b
     */
    
    //1
    Map<Token, Vertex> newVertices = mergeTokens(graph, a, Collections.<Token, Vertex> emptyMap());
    //2
    Map<VectorSpace.Vector, List<VariantGraph.Vertex>> vrvx = putAllTheVerticesForThisWitnessInVectorMap(a, dimensionA, dimensionB, newVertices);
    //3
    
    // fetch all vectors for witness B
    List<Vector> vectorsWitnessB = findAllVectorForADimension(1, 0);
    // the idea here is to add vector after vector to the VG.
    // most important first (meaning: sorted on length, depth)
    // this ways causes the least amount of transpositions.
    Map<Token, VariantGraph.Vertex> al = Maps.newHashMap();
    for (Vector v: vectorsWitnessB) {
      List<Token> tokensFromVector = getTokensFromVector(v, 1, b);
      List<Vertex> vertices = getVerticesFromVector(v, vrvx);
      for (int i=0; i < tokensFromVector.size(); i++) {
        al.put(tokensFromVector.get(i), vertices.get(i));
      }
    }
    mergeTokens(graph, b, al);
  }

  private List<Vertex> getVerticesFromVector(Vector v, Map<Vector, List<Vertex>> vrvx) {
    if (!vrvx.containsKey(v)) {
      throw new RuntimeException("Vector "+v+" was expected to be present in the map, but wasn't!");
    }
    return vrvx.get(v);
  }

  private Map<VectorSpace.Vector, List<VariantGraph.Vertex>> putAllTheVerticesForThisWitnessInVectorMap(Iterable<Token> a, int dimensionA, int dimensionB, Map<Token, Vertex> newVertices) {
    // first we have to select all the vectors from the vectorspace
    // that are present in the dimensions of witness a & b
    List<Vector> vs = findAllVectorForADimension(dimensionA, dimensionB);
    // put vertices by the vector
    // dit doe ik aan de hand van witness a
    Map<VectorSpace.Vector, List<VariantGraph.Vertex>> vrvx = Maps.newHashMap();
    // TODO: dit moet een multimap worden
    int counterToken = 0;
    for (Token t: a) {
      counterToken++;
      for (VectorSpace.Vector v : vs) {
        if (counterToken >= v.startCoordinate[dimensionA] && counterToken <= (v.startCoordinate[dimensionA]+v.length-1)) {
          // get the vertex for this token
          VariantGraph.Vertex vx = newVertices.get(t);
          if (vrvx.containsKey(v)) {
            List<VariantGraph.Vertex> ex = vrvx.get(v);
            ex.add(vx);
          } else {
            List<VariantGraph.Vertex> ne = Lists.newArrayList();
            ne.add(vx);
            vrvx.put(v, ne);
          }
        }
      }
    }
    return vrvx;
  }
  
  //TODO: dimensionB should be removed
  // it should be present in dimension A and a dimension higher or
  // lower
  private List<Vector> findAllVectorForADimension(int dimensionA, int dimensionB) {
    List<Vector> vs = Lists.newArrayList();
    for (VectorSpace.Vector v : s.getVectors()) {
      //check whether this vector is present in both dimensions
      if (v.isPresentIn(dimensionA)&&v.isPresentIn(dimensionB)) {
        vs.add(v);
      }
    }
    return vs;
  }

}
