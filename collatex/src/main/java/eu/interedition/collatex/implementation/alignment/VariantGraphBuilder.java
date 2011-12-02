package eu.interedition.collatex.implementation.alignment;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import eu.interedition.collatex.implementation.Tuple;
import eu.interedition.collatex.implementation.graph.db.PersistentVariantGraph;
import eu.interedition.collatex.implementation.graph.db.PersistentVariantGraphVertex;
import eu.interedition.collatex.implementation.matching.EqualityTokenComparator;
import eu.interedition.collatex.interfaces.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class VariantGraphBuilder {
  private static final Logger LOG = LoggerFactory.getLogger(VariantGraphBuilder.class);

  private final PersistentVariantGraph graph;
  private final Comparator<INormalizedToken> comparator;
  private final ITokenLinker tokenLinker;
  private final PhraseMatchDetector phraseMatchDetector;
  private final TranspositionDetector transpositionDetector;

  private Map<INormalizedToken,INormalizedToken> tokenLinks;
  private List<Tuple<List<INormalizedToken>>> phraseMatches;
  private List<Tuple<List<INormalizedToken>>> transpositions;
  private Map<INormalizedToken,INormalizedToken> alignments;

  public VariantGraphBuilder(PersistentVariantGraph graph) {
    this(graph, new EqualityTokenComparator(), new TokenLinker(), new PhraseMatchDetector(), new TranspositionDetector());
  }

  public VariantGraphBuilder(PersistentVariantGraph graph, Comparator<INormalizedToken> comparator, ITokenLinker tokenLinker, PhraseMatchDetector phraseMatchDetector, TranspositionDetector transpositionDetector) {
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

  public List<Tuple<List<INormalizedToken>>> getTranspositions() {
    return Collections.unmodifiableList(transpositions);
  }

  public Map<INormalizedToken, INormalizedToken> getAlignments() {
    return Collections.unmodifiableMap(alignments);
  }

  protected void merge(IWitness witness) {
    final IWitness base = VariantGraphWitnessAdapter.create(graph);

    if (LOG.isTraceEnabled()) {
      LOG.trace("{} + {}: {} vs. {}", new Object[] { graph, witness, base.getTokens(), witness.getTokens() });
    }

    LOG.debug("{} + {}: Match and link tokens", graph, witness);
    tokenLinks = tokenLinker.link(base, witness, comparator);
    if (LOG.isTraceEnabled()) {
      for (Map.Entry<INormalizedToken, INormalizedToken> tokenLink : tokenLinks.entrySet()) {
        LOG.trace("{} + {}: Token match: {} = {}", new Object[] { graph, witness, tokenLink.getValue(), tokenLink.getKey() });
      }
    }

    LOG.debug("{} + {}: Detect phrase matches", graph, witness);
    phraseMatches = phraseMatchDetector.detect(tokenLinks, base, witness);
    if (LOG.isTraceEnabled()) {
      for (Tuple<List<INormalizedToken>> phraseMatch : phraseMatches) {
        LOG.trace("{} + {}: Phrase match: {} = {}", new Object[] { graph, witness, Iterables.toString(phraseMatch.left), Iterables.toString(phraseMatch.right) });
      }
    }

    LOG.debug("{} + {}: Detect transpositions", graph, witness);
    transpositions = filterMirrored(transpositionDetector.detect(phraseMatches, base), witness);
    if (LOG.isTraceEnabled()) {
      for (Tuple<List<INormalizedToken>> transposition : transpositions) {
        LOG.trace("{} + {}: Transposition: {} = {}", new Object[] { graph, witness, Iterables.toString(transposition.left), Iterables.toString(transposition.right) });
      }
    }

    LOG.debug("{} + {}: Determine aligned tokens by filtering transpositions", graph, witness);
    alignments = Maps.newLinkedHashMap(tokenLinks);
    for (Tuple<List<INormalizedToken>> transposedPhrase : transpositions) {
      alignments.keySet().removeAll(transposedPhrase.right);
    }
    if (LOG.isTraceEnabled()) {
      for (Map.Entry<INormalizedToken, INormalizedToken> alignment : alignments.entrySet()) {
        LOG.trace("{} + {}: Alignment: {} = {}", new Object[] { graph, witness, alignment.getValue(), alignment.getKey() });
      }
    }

    LOG.debug("{} + {}: Merge comparand into graph", graph, witness);
    PersistentVariantGraphVertex last = graph.getStart();
    final TreeSet<IWitness> witnessSet = Sets.newTreeSet(Collections.singleton(witness));
    for (INormalizedToken token : witness.getTokens()) {
      final VariantGraphWitnessAdapter.VariantGraphVertexTokenAdapter matchingAdapter = (VariantGraphWitnessAdapter.VariantGraphVertexTokenAdapter)alignments.get(token);
      PersistentVariantGraphVertex matchingVertex = (matchingAdapter == null ? null : matchingAdapter.getVertex());
      if (matchingVertex == null) {
        matchingVertex = graph.addVertex(token);
      } else {
        matchingVertex.add(Collections.singleton(token));
      }
      graph.createPath(last, matchingVertex, witnessSet);
      last = matchingVertex;
    }
    graph.createPath(last, graph.getEnd(), witnessSet);

    // FIXME: register transpositions in graph!

    if (LOG.isTraceEnabled()) {
      LOG.trace("{}: {}", graph, Iterables.toString(graph.traverseVertices(null)));
    }
  }

  // NOTE: this method should not return the original sequence when a mirror exists!
  private List<Tuple<List<INormalizedToken>>> filterMirrored(List<Tuple<Tuple<List<INormalizedToken>>>> transpositions, IWitness witness) {
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
