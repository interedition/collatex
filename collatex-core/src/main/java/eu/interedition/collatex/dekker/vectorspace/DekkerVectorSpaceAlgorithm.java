package eu.interedition.collatex.dekker.vectorspace;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.collect.Sets;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
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
    build(graph, a, b, c, 0, 1, 2);
  }

  private void addNewTransposition(List<Match> phrase, Stack<List<Match>> transpositions) {
    LOG.fine("Transposition found! "+phrase);
    transpositions.add(phrase);
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
      if (!it.hasNext()) {
        throw new RuntimeException("No more tokens; expected "+v.length+" tokens, but was: "+Iterables.size(a));
      }
      Token t = it.next();
      tokens.add(t);
    }
    return tokens;
  }
  
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
  public void build(VariantGraph graph, Iterable<Token> a, Iterable<Token> b, Iterable<Token> c, int dimensionA, final int dimensionB, int dimensionC) {
    //1
    Map<Token, Vertex> newVertices = mergeTokens(graph, a, Collections.<Token, Vertex> emptyMap());
    //2
    List<Vector> vectorToDoA = s.getAllVectorsWithAMinimumDImension(dimensionA);
     Map<VectorSpace.Vector, List<VariantGraph.Vertex>> vrvx = generateVectorToVertexMap(a, 0, newVertices, vectorToDoA);
    //3
    
    // add witness b to the graph
    newVertices = addCollationResultToGraph(graph, b, dimensionB, vrvx);
    // find all the vertices that are present in the dimension of b and have b as the minimal dimension
    // if vertices are not present in the vrvx map it should be added
    List<Vector> vectorToDo = s.getAllVectorsWithAMinimumDImension(dimensionB);
    vrvx.putAll(generateVectorToVertexMap(b, dimensionB, newVertices, vectorToDo));
    // add witness c to the graph
    if (!Iterables.isEmpty(c)) {
      addCollationResultToGraph(graph, c, dimensionC, vrvx);
    }
  }

  private Map<Token, Vertex> addCollationResultToGraph(VariantGraph graph, Iterable<Token> b, final int dimension, Map<VectorSpace.Vector, List<VariantGraph.Vertex>> vrvx) {
    LOG.fine("Adding "+dimension+" to graph.");
    // fetch all vectors for witness B
    // List<Vector> vs = s.getAllVectorsWithAMinimumDImension(dimensionB);
    List<Vector> vsWithDimension = s.getVectorsWithDimension(dimension);
    List<Vector> vsWithMaxDimension = s.getAllVectorsWithMaximumDimension(dimension);
    Set<Vector> vsSet = Sets.newHashSet();
    vsSet.addAll(vsWithDimension);
    vsSet.retainAll(vsWithMaxDimension);
    /* the idea here is to add vector after vector to the VG.
    * most important first (meaning: sorted on length, depth)
    * this ways causes the least amount of transpositions.
    * we order the vectors by their coordinate in dimension B
    */
    List<Vector> vs = Lists.newArrayList(vsSet);
    Collections.sort(vs, new Comparator<Vector>(){
      @Override
      public int compare(Vector v1, Vector v2) {
        return v1.startCoordinate[dimension] - v2.startCoordinate[dimension];
      }});
    List<List<Match>> phrases = generatePhrasesFromVectors(b, vrvx, vs, dimension);
    final Stack<List<Match>> transpositions = detectTranspositions(graph, phrases);
    Map<Token, Vertex> newVertices = mergeWitnessIntoGraph(graph, b, phrases, transpositions);
    return newVertices;
  }

  private List<List<Match>> generatePhrasesFromVectors(Iterable<Token> witness, Map<VectorSpace.Vector, List<VariantGraph.Vertex>> vrvx, List<Vector> vs, int dimension) {
    // now we have to check the order of the vectors to add
    // with the order in the variant graph
    // for this purpose we look at the ranking of the graph
    // for the first vertices of each of the vectors.
    List<List<Match>> phrases = Lists.newArrayList();
    for (Vector v: vs) {
      // build a phrase for each vector
      List<Match> phrase = Lists.newArrayList();
      List<Token> tokensFromVector = getTokensFromVector(v, dimension, witness);
      List<Vertex> verticesFromVector = getVerticesFromVector(v, vrvx);
      for (int i=0; i< v.length; i++) {
        Vertex vx = verticesFromVector.get(i);
        Token tokenWitness = tokensFromVector.get(i);
        phrase.add(new Match(vx, tokenWitness));
      }
      phrases.add(phrase);
    }
    return phrases;
  }

  private Stack<List<Match>> detectTranspositions(VariantGraph graph, List<List<Match>> phrases) {
    Set<VariantGraph.Vertex> firstVertices = Sets.newHashSet();
    for (List<Match> phrase : phrases) {
      firstVertices.add(phrase.get(0).vertex);
    }
    // prepare for transposition detection
    // rank vertices
    VariantGraphRanking ranking = VariantGraphRanking.ofOnlyCertainVertices(graph, null, firstVertices);
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
    return transpositions;
  }

  private Map<Token, Vertex> mergeWitnessIntoGraph(VariantGraph graph, Iterable<Token> witness, List<List<Match>> phrases, final Stack<List<Match>> transpositions) {
    // convert the phrases to a alignment map
    Map<Token, VariantGraph.Vertex> al = Maps.newHashMap();
    for (List<Match> phrase : phrases) {  
      for (int i=0; i < phrase.size(); i++) {
        al.put(phrase.get(i).token, phrase.get(i).vertex);
      }
    }
    // remove transposed vertices from the alignment map
    for (List<Match> t : transpositions) {
      for (Match m : t) {
        al.remove(m.token);
      }
    }
    // and merge tokens and transpositions in
    Map<Token, Vertex> newVertices = mergeTokens(graph, witness, al);
    mergeTranspositions(graph, transpositions);
    return newVertices;
  }

  private List<Vertex> getVerticesFromVector(Vector v, Map<Vector, List<Vertex>> vrvx) {
    if (!vrvx.containsKey(v)) {
      throw new RuntimeException("Vector "+v+" was expected to be present in the map, but wasn't!");
    }
    return vrvx.get(v);
  }

  private Map<VectorSpace.Vector, List<VariantGraph.Vertex>> generateVectorToVertexMap(Iterable<Token> witness, int dimension, Map<Token, Vertex> newVertices, List<Vector> vs) {
    // put vertices by the vector
    // dit doe ik aan de hand van witness a
    Map<VectorSpace.Vector, List<VariantGraph.Vertex>> vrvx = Maps.newHashMap();
    // TODO: dit moet een multimap worden
    int counterToken = 0;
    for (Token t: witness) {
      counterToken++;
      for (VectorSpace.Vector v : vs) {
        if (counterToken >= v.startCoordinate[dimension] && counterToken <= (v.startCoordinate[dimension]+v.length-1)) {
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

}
