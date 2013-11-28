package eu.interedition.collatex.dekker.vectorspace;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.VariantGraph.Vertex;
import eu.interedition.collatex.dekker.Match;
import eu.interedition.collatex.dekker.TranspositionDetector;
import eu.interedition.collatex.dekker.TranspositionFilter;
import eu.interedition.collatex.dekker.vectorspace.VectorSpace.Vector;

public class VSToVGBuilder {
  private final Logger LOG = Logger.getLogger(getClass().getName());
  private final TokenVectorSpace s;
  
  public VSToVGBuilder(TokenVectorSpace s) {
    this.s = s;
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
    // fetch all vectors for witness
    List<Vector> vs = s.getAllVectorsContainingDimensionButNotAsMinimum(dimension);
    LOG.fine("Vector(s) to add for dimension "+dimension+": "+vs);
    /* the idea here is to add vector after vector to the VG.
    * most important first (meaning: sorted on length, depth)
    * this ways causes the least amount of transpositions.
    * we order the vectors by their coordinate in dimension B
    */
    Collections.sort(vs, new Comparator<Vector>(){
      @Override
      public int compare(Vector v1, Vector v2) {
        return v1.startCoordinate[dimension] - v2.startCoordinate[dimension];
      }});
    List<List<Match>> phrases = generatePhrasesFromVectors(b, vrvx, vs, dimension);
    TranspositionDetector detector = new TranspositionDetector();
    final List<List<Match>> transpositions = detector.detect(phrases, graph);
    Map<Token, Vertex> newVertices = mergeWitnessIntoGraph(graph, b, phrases, transpositions);
    return newVertices;
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

  private Map<Token, Vertex> mergeWitnessIntoGraph(VariantGraph graph, Iterable<Token> witness, List<List<Match>> phrases, final List<List<Match>> transpositions) {
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
    // and merge tokens in
    Map<Token, Vertex> newVertices = mergeTokens(graph, witness, al);
    // filter away moves that humans would not regard as transpositions
    TranspositionFilter.filter(graph, transpositions, newVertices);
    // merge transpositions in
    mergeTranspositions(graph, transpositions, newVertices);
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
  
  private List<Vertex> getVerticesFromVector(Vector v, Map<Vector, List<Vertex>> vrvx) {
    if (!vrvx.containsKey(v)) {
      throw new RuntimeException("Vector "+v+" was expected to be present in the map, but wasn't!");
    }
    return vrvx.get(v);
  }
  
  //TODO: COPIED from CollationAlgorithm.Base class!!!
  protected Map<Token, Vertex> mergeTokens(VariantGraph into, Iterable<Token> witnessTokens, Map<Token, VariantGraph.Vertex> alignments) {
    Preconditions.checkArgument(!Iterables.isEmpty(witnessTokens), "Empty witness");
    final Witness witness = Iterables.getFirst(witnessTokens, null).getWitness();

    if (LOG.isLoggable(Level.FINE)) {
      LOG.log(Level.FINE, "{0} + {1}: Merge comparand into graph", new Object[] { into, witness });
    }
    Map<Token, VariantGraph.Vertex> witnessTokenVertices = Maps.newHashMap();
    VariantGraph.Vertex last = into.getStart();
    final Set<Witness> witnessSet = Collections.singleton(witness);
    for (Token token : witnessTokens) {
      VariantGraph.Vertex matchingVertex = alignments.get(token);
      if (matchingVertex == null) {
        matchingVertex = into.add(token);
      } else {
        if (LOG.isLoggable(Level.FINE)) {
          LOG.log(Level.FINE, "Match: {0} to {1}", new Object[] { matchingVertex, token });
        }
        matchingVertex.add(Collections.singleton(token));
      }
      witnessTokenVertices.put(token, matchingVertex);

      into.connect(last, matchingVertex, witnessSet);
      last = matchingVertex;
    }
    into.connect(last, into.getEnd(), witnessSet);
    return witnessTokenVertices;
  }

  //TODO: COPIED from CollationAlgorithm.Base class!!!
  protected void mergeTranspositions(VariantGraph into, List<List<Match>> transpositions, Map<Token, VariantGraph.Vertex> witnessTokenVertices) {
    for (List<Match> transposedPhrase : transpositions) {
      if (LOG.isLoggable(Level.FINE)) {
        LOG.log(Level.FINE, "Transposition: {0}", transposedPhrase);
      }
      final Set<VariantGraph.Vertex> transposed = Sets.newHashSet();
      for (Match match : transposedPhrase) {
        transposed.add(witnessTokenVertices.get(match.token));
        transposed.add(match.vertex);
      }
      into.transpose(transposed);
    }
  }

}
