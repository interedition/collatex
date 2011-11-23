package eu.interedition.collatex2.implementation.vg_alignment;

import java.util.*;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex2.implementation.Tuple;
import eu.interedition.collatex2.implementation.containers.graph.VariantGraphEdge;
import eu.interedition.collatex2.implementation.containers.graph.VariantGraphVertex;
import eu.interedition.collatex2.implementation.input.NormalizedToken;
import eu.interedition.collatex2.implementation.vg_analysis.*;
import eu.interedition.collatex2.interfaces.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VariantGraphBuilder {
  private static final Logger LOG = LoggerFactory.getLogger(VariantGraphBuilder.class);

  private final IVariantGraph graph;
  private final ITokenLinker tokenLinker;
  private final SequenceDetector sequenceDetector;
  private final TranspositionDetector transpositionDetector;

  private Map<INormalizedToken,INormalizedToken> tokenLinks;
  private List<Tuple<List<INormalizedToken>>> sequences;
  private List<ITransposition> transpositions;
  private Map<INormalizedToken,INormalizedToken> alignments;

  public VariantGraphBuilder(IVariantGraph graph) {
    this(graph, new TokenLinker(), new SequenceDetector(), new TranspositionDetector());
  }

  public VariantGraphBuilder(IVariantGraph graph, ITokenLinker tokenLinker, SequenceDetector sequenceDetector, TranspositionDetector transpositionDetector) {
    this.graph = graph;
    this.tokenLinker = tokenLinker;
    this.sequenceDetector = sequenceDetector;
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

  public List<Tuple<List<INormalizedToken>>> getSequences() {
    return Collections.unmodifiableList(sequences);
  }

  public List<ITransposition> getTranspositions() {
    return Collections.unmodifiableList(transpositions);
  }

  public Map<INormalizedToken, INormalizedToken> getAlignments() {
    return Collections.unmodifiableMap(alignments);
  }

  protected void merge(IWitness witness) {
    final IWitness base = new Superbase(graph);

    LOG.debug("{} + {}: Match and link tokens", graph, witness);
    tokenLinks = tokenLinker.link(base, witness);

    LOG.debug("{} + {}: Detect sequences", graph, witness);
    sequences = sequenceDetector.detect(tokenLinks, base, witness);

    LOG.debug("{} + {}: Detect transpositions of sequences", graph, witness);
    transpositions = transpositionDetector.detect(sequences, base);

    LOG.debug("{} + {}: Filter aligned tokens", graph, witness);
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
  private List<Tuple<List<INormalizedToken>>> findTransposedSequences(List<ITransposition> transpositions, IWitness witness) {
    final List<Tuple<List<INormalizedToken>>> transposed = Lists.newArrayList();
    final Deque<ITransposition> toCheck = new ArrayDeque<ITransposition>(transpositions);
    while (!toCheck.isEmpty()) {
      final ITransposition current = toCheck.pop();
      final ITransposition mirrored = findMirroredTransposition(toCheck, current);
      if (mirrored != null && transpositionsAreNear(current, mirrored, witness)) {
        toCheck.remove(mirrored);
        transposed.add(mirrored.getSequenceA());
      } else {
        transposed.add(current.getSequenceA());
      }
    }
    return transposed;
  }

  private ITransposition findMirroredTransposition(final Deque<ITransposition> transToCheck, final ITransposition original) {
    for (final ITransposition transposition : transToCheck) {
      if (equals(transposition.getSequenceA(), original.getSequenceB())) {
        if (equals(transposition.getSequenceB(), original.getSequenceA())) {
          return transposition;
        }
      }
    }
    return null;
  }

  /**
   * @deprecated This does not work with a custom matching function.
   */
  @Deprecated
  private boolean equals(Tuple<List<INormalizedToken>> a, Tuple<List<INormalizedToken>> b) {
    return NormalizedToken.toString(a.right).equals(NormalizedToken.toString(b.right));
  }

  // Note: this only calculates the distance between the tokens in the witness.
  // Note: it does not take into account a possible distance in the vertices in the graph!
  private boolean transpositionsAreNear(ITransposition a, ITransposition b, IWitness witness) {
    return witness.isNear(Iterables.getLast(a.getSequenceB().right), Iterables.get(b.getSequenceB().right, 0));
  }

}
