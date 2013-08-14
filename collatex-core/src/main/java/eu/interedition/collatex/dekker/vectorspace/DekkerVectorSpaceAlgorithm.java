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
    createVariantGraph(graph, a, b);
  }

  private void createVariantGraph(VariantGraph graph, SimpleWitness a, SimpleWitness b) {
    if (!graph.witnesses().isEmpty()) {
      throw new RuntimeException("Graph is not empty! This algorithm is not a progressive algorithm.");
    }
    // merge the first witness in
    // there are no alignments
    merge(graph, a, Collections.<Token, VariantGraph.Vertex> emptyMap());
    
    Map<Token, Token> alignments = fillTokenTokenAlignmentsMap(a, b);

    // first we have to select all the vectors from the vectorspace
    // that are present in the dimensions of witness a & b
    List<Vector> vs = Lists.newArrayList();
    for (VectorSpace.Vector v : s.getVectors()) {
      //check whether this vector is present in both dimensions
      if (v.isPresentIn(0)&&v.isPresentIn(1)) {
        vs.add(v);
      }
    }
    // now we have to check the order of the vectors to add
    // with the order in the variant graph
    // for this purpose we look at the ranking of the graph
    // for the first vertices of each of the vectors.
    List<List<Match>> phrases = Lists.newArrayList();
    Set<VariantGraph.Vertex> firstVertices = Sets.newHashSet();
    for (Vector v: vs) {
      List<Match> phrase = Lists.newArrayList();
      for (int i=0; i< v.length; i++) {
        Token tokenGraph = getTokensFromVector(v, 0, a).get(i);
        Token tokenWitness = getTokensFromVector(v, 1, b).get(i);
        VariantGraph.Vertex vx = witnessTokenVertices.get(tokenGraph);
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
    mergeTokens(graph, b, alignments);
    
    mergeTranspositions(graph, transpositions);
  }

  private void addNewTransposition(List<Match> phrase, Stack<List<Match>> transpositions) {
    LOG.fine("Transposition found! "+phrase);
    transpositions.add(phrase);
  }

  // convert vectors to <Token, Token> (Witness, other)
  private Map<Token, Token> fillTokenTokenAlignmentsMap(SimpleWitness a, SimpleWitness b) {
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
}
