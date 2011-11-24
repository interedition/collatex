package eu.interedition.collatex2.implementation.alignment;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import eu.interedition.collatex2.implementation.Tuple;
import eu.interedition.collatex2.implementation.graph.VariantGraphEdge;
import eu.interedition.collatex2.implementation.graph.VariantGraphVertex;
import eu.interedition.collatex2.implementation.matching.EqualityTokenComparator;
import eu.interedition.collatex2.interfaces.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class VariantGraphBuilder {
  private static final Logger LOG = LoggerFactory.getLogger(VariantGraphBuilder.class);

  private final IVariantGraph graph;
  private final Comparator<INormalizedToken> comparator;
  private final ITokenLinker tokenLinker;
  private final PhraseMatchDetector phraseMatchDetector;
  private final TranspositionDetector transpositionDetector;

  private Map<INormalizedToken,INormalizedToken> tokenLinks;
  private List<Tuple<List<INormalizedToken>>> phraseMatches;
  private List<Tuple<Tuple<List<INormalizedToken>>>> transpositions;
  private Map<INormalizedToken,INormalizedToken> alignments;

  public VariantGraphBuilder(IVariantGraph graph) {
    this(graph, new EqualityTokenComparator(), new TokenLinker(), new PhraseMatchDetector(), new TranspositionDetector());
  }

  public VariantGraphBuilder(IVariantGraph graph, Comparator<INormalizedToken> comparator, ITokenLinker tokenLinker, PhraseMatchDetector phraseMatchDetector, TranspositionDetector transpositionDetector) {
    this.graph = graph;
    this.comparator = comparator;
    this.tokenLinker = tokenLinker;
    this.phraseMatchDetector = phraseMatchDetector;
    this.transpositionDetector = transpositionDetector;
  }

  public VariantGraphBuilder add(IWitness... witnesses) {
    for (IWitness witness : witnesses) {
      merge(witness);
    }
    return this;
  }

  public Map<INormalizedToken, INormalizedToken> getTokenLinks() {
    return tokenLinks;
  }

  public List<Tuple<List<INormalizedToken>>> getPhraseMatches() {
    return Collections.unmodifiableList(phraseMatches);
  }

  public List<Tuple<Tuple<List<INormalizedToken>>>> getTranspositions() {
    return Collections.unmodifiableList(transpositions);
  }

  public Map<INormalizedToken, INormalizedToken> getAlignments() {
    return Collections.unmodifiableMap(alignments);
  }

  protected void merge(IWitness witness) {
    final IWitness base = VariantGraphWitnessAdapter.create(graph);

    LOG.debug("{} + {}: Match and link tokens", graph, witness);
    tokenLinks = tokenLinker.link(base, witness, comparator);

    LOG.debug("{} + {}: Detect phrase matches", graph, witness);
    phraseMatches = phraseMatchDetector.detect(tokenLinks, base, witness);

    LOG.debug("{} + {}: Detect transpositions", graph, witness);
    transpositions = transpositionDetector.detect(phraseMatches, base);

    LOG.debug("{} + {}: Determine aligned tokens by filtering transpositions", graph, witness);
    alignments = Maps.newLinkedHashMap(tokenLinks);
    for (Tuple<List<INormalizedToken>> transposedSequence : findTransposedSequences(transpositions, witness)) {
      alignments.keySet().removeAll(transposedSequence.right);
    }

    LOG.debug("{} + {}: Merge comparand into graph", graph, witness);
    IVariantGraphVertex last =  graph.getStartVertex();
    for (INormalizedToken token : witness.getTokens()) {
      final IVariantGraphVertex graphMatch = (IVariantGraphVertex) tokenLinks.get(token);
      final INormalizedToken vertexKey = (graphMatch == null ? token : graphMatch.getVertexKey());

      IVariantGraphVertex vertex = alignments.containsKey(token) ? graphMatch : null;
      if (vertex == null) {
        graph.addVertex(vertex = new VariantGraphVertex(token.getNormalized(), vertexKey));
      }

      IVariantGraphEdge edge = graph.getEdge(last, vertex);
      if (edge == null) {
        graph.addEdge(last, vertex, edge = new VariantGraphEdge());
      }

      vertex.addToken(witness, token);
      edge.addWitness(witness);
      last = vertex;
    }

    IVariantGraphEdge edge = graph.getEdge(last, graph.getEndVertex());
    if (edge == null) {
      graph.addEdge(last, graph.getEndVertex(), edge = new VariantGraphEdge());
    }
    edge.addWitness(witness);
  }

  // NOTE: this method should not return the original sequence when a mirror exists!
  private List<Tuple<List<INormalizedToken>>> findTransposedSequences(List<Tuple<Tuple<List<INormalizedToken>>>> transpositions, IWitness witness) {
    final List<Tuple<List<INormalizedToken>>> transposed = Lists.newArrayList();
    final Deque<Tuple<Tuple<List<INormalizedToken>>>> toCheck = new ArrayDeque<Tuple<Tuple<List<INormalizedToken>>>>(transpositions);
    while (!toCheck.isEmpty()) {
      final Tuple<Tuple<List<INormalizedToken>>> current = toCheck.pop();
      final Tuple<Tuple<List<INormalizedToken>>> mirrored = findMirroredTransposition(toCheck, current);
      if (mirrored != null && transpositionsAreNear(current, mirrored, witness)) {
        toCheck.remove(mirrored);
        transposed.add(mirrored.left);
      } else {
        transposed.add(current.left);
      }
    }
    return transposed;
  }

  private Tuple<Tuple<List<INormalizedToken>>> findMirroredTransposition(final Deque<Tuple<Tuple<List<INormalizedToken>>>> transToCheck, final Tuple<Tuple<List<INormalizedToken>>> original) {
    for (final Tuple<Tuple<List<INormalizedToken>>> transposition : transToCheck) {
      if (equals(transposition.left, original.right)) {
        if (equals(transposition.right, original.left)) {
          return transposition;
        }
      }
    }
    return null;
  }

  private boolean equals(Tuple<List<INormalizedToken>> a, Tuple<List<INormalizedToken>> b) {
    if (a.right.size() != b.right.size()) {
      return false;
    }
    final Iterator<INormalizedToken> aIt = a.right.iterator();
    final Iterator<INormalizedToken> bIt = b.right.iterator();
    while (aIt.hasNext() && bIt.hasNext()) {
      if (comparator.compare(aIt.next(), bIt.next()) != 0) {
        return false;
      }
    }
    return true;
  }

  // Note: this only calculates the distance between the tokens in the witness.
  // Note: it does not take into account a possible distance in the vertices in the graph!
  private boolean transpositionsAreNear(Tuple<Tuple<List<INormalizedToken>>> a, Tuple<Tuple<List<INormalizedToken>>> b, IWitness witness) {
    return witness.isNear(Iterables.getLast(a.right.right), Iterables.get(b.right.right, 0));
  }

}
