package eu.interedition.collatex.implementation.alignment;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import eu.interedition.collatex.implementation.Tuple;
import eu.interedition.collatex.implementation.graph.db.VariantGraph;
import eu.interedition.collatex.implementation.graph.db.VariantGraphVertex;
import eu.interedition.collatex.implementation.matching.EqualityTokenComparator;
import eu.interedition.collatex.interfaces.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class VariantGraphBuilder {
  private static final Logger LOG = LoggerFactory.getLogger(VariantGraphBuilder.class);

  private final VariantGraph graph;
  private final Comparator<Token> comparator;
  private final ITokenLinker tokenLinker;
  private final PhraseMatchDetector phraseMatchDetector;
  private final TranspositionDetector transpositionDetector;

  private Map<Token, Token> tokenLinks;
  private List<Tuple<List<Token>>> phraseMatches;
  private List<Tuple<List<Token>>> transpositions;
  private Map<Token,Token> alignments;

  public VariantGraphBuilder(VariantGraph graph) {
    this(graph, new EqualityTokenComparator(), new TokenLinker(), new PhraseMatchDetector(), new TranspositionDetector());
  }

  public VariantGraphBuilder(VariantGraph graph, Comparator<Token> comparator, ITokenLinker tokenLinker, PhraseMatchDetector phraseMatchDetector, TranspositionDetector transpositionDetector) {
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

  public Map<Token, Token> getTokenLinks() {
    return tokenLinks;
  }

  public List<Tuple<List<Token>>> getPhraseMatches() {
    return Collections.unmodifiableList(phraseMatches);
  }

  public List<Tuple<List<Token>>> getTranspositions() {
    return Collections.unmodifiableList(transpositions);
  }

  public Map<Token, Token> getAlignments() {
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
      for (Map.Entry<Token, Token> tokenLink : tokenLinks.entrySet()) {
        LOG.trace("{} + {}: Token match: {} = {}", new Object[] { graph, witness, tokenLink.getValue(), tokenLink.getKey() });
      }
    }

    LOG.debug("{} + {}: Detect phrase matches", graph, witness);
    phraseMatches = phraseMatchDetector.detect(tokenLinks, base, witness);
    if (LOG.isTraceEnabled()) {
      for (Tuple<List<Token>> phraseMatch : phraseMatches) {
        LOG.trace("{} + {}: Phrase match: {} = {}", new Object[] { graph, witness, Iterables.toString(phraseMatch.left), Iterables.toString(phraseMatch.right) });
      }
    }

    LOG.debug("{} + {}: Detect transpositions", graph, witness);
    transpositions = filterMirrored(transpositionDetector.detect(phraseMatches, base), witness);
    if (LOG.isTraceEnabled()) {
      for (Tuple<List<Token>> transposition : transpositions) {
        LOG.trace("{} + {}: Transposition: {} = {}", new Object[] { graph, witness, Iterables.toString(transposition.left), Iterables.toString(transposition.right) });
      }
    }

    LOG.debug("{} + {}: Determine aligned tokens by filtering transpositions", graph, witness);
    alignments = Maps.newLinkedHashMap(tokenLinks);
    for (Tuple<List<Token>> transposedPhrase : transpositions) {
      alignments.keySet().removeAll(transposedPhrase.right);
    }
    if (LOG.isTraceEnabled()) {
      for (Map.Entry<Token, Token> alignment : alignments.entrySet()) {
        LOG.trace("{} + {}: Alignment: {} = {}", new Object[] { graph, witness, alignment.getValue(), alignment.getKey() });
      }
    }

    LOG.debug("{} + {}: Merge comparand into graph", graph, witness);
    VariantGraphVertex last = graph.getStart();
    final SortedSet<IWitness> witnessSet = Sets.newTreeSet(Collections.singleton(witness));
    final Map<Token, VariantGraphVertex> witnessTokenVertices = Maps.newHashMap();
    for (Token token : witness.getTokens()) {
      final VariantGraphWitnessAdapter.VariantGraphVertexTokenAdapter matchingAdapter = (VariantGraphWitnessAdapter.VariantGraphVertexTokenAdapter)alignments.get(token);
      VariantGraphVertex matchingVertex = (matchingAdapter == null ? null : matchingAdapter.getVertex());
      if (matchingVertex == null) {
        matchingVertex = graph.add(token);
      } else {
        matchingVertex.add(Collections.singleton(token));
      }
      witnessTokenVertices.put(token, matchingVertex);

      graph.connect(last, matchingVertex, witnessSet);
      last = matchingVertex;
    }
    graph.connect(last, graph.getEnd(), witnessSet);

    LOG.debug("{}: Registering transpositions", graph);
    for (Tuple<List<Token>> transposedPhrase : transpositions) {
      final Iterator<Token> basePhraseIt = transposedPhrase.left.iterator();
      final Iterator<Token> witnessPhraseIt = transposedPhrase.right.iterator();

      while(basePhraseIt.hasNext() && witnessPhraseIt.hasNext()) {
        final VariantGraphVertex baseVertex = ((VariantGraphWitnessAdapter.VariantGraphVertexTokenAdapter)basePhraseIt.next()).getVertex();
        final VariantGraphVertex witnessVertex = witnessTokenVertices.get(witnessPhraseIt.next());
        graph.transpose(baseVertex, witnessVertex);
      }
    }

    if (LOG.isTraceEnabled()) {
      LOG.trace("{}: {}", graph, Iterables.toString(graph.vertices()));
    }
  }

  // NOTE: this method should not return the original sequence when a mirror exists!
  private List<Tuple<List<Token>>> filterMirrored(List<Tuple<Tuple<List<Token>>>> transpositions, IWitness witness) {
    final List<Tuple<List<Token>>> transposed = Lists.newArrayList();
    final Deque<Tuple<Tuple<List<Token>>>> toCheck = new ArrayDeque<Tuple<Tuple<List<Token>>>>(transpositions);
    while (!toCheck.isEmpty()) {
      final Tuple<Tuple<List<Token>>> current = toCheck.pop();
      final Tuple<Tuple<List<Token>>> mirrored = findMirroredTransposition(toCheck, current);
      if (mirrored != null && transpositionsAreNear(current, mirrored, witness)) {
        toCheck.remove(mirrored);
        transposed.add(mirrored.left);
      } else {
        transposed.add(current.left);
      }
    }
    return transposed;
  }

  private Tuple<Tuple<List<Token>>> findMirroredTransposition(final Deque<Tuple<Tuple<List<Token>>>> transToCheck, final Tuple<Tuple<List<Token>>> original) {
    for (final Tuple<Tuple<List<Token>>> transposition : transToCheck) {
      if (equals(transposition.left, original.right)) {
        if (equals(transposition.right, original.left)) {
          return transposition;
        }
      }
    }
    return null;
  }

  private boolean equals(Tuple<List<Token>> a, Tuple<List<Token>> b) {
    if (a.right.size() != b.right.size()) {
      return false;
    }
    final Iterator<Token> aIt = a.right.iterator();
    final Iterator<Token> bIt = b.right.iterator();
    while (aIt.hasNext() && bIt.hasNext()) {
      if (comparator.compare(aIt.next(), bIt.next()) != 0) {
        return false;
      }
    }
    return true;
  }

  // Note: this only calculates the distance between the tokens in the witness.
  // Note: it does not take into account a possible distance in the vertices in the graph!
  private boolean transpositionsAreNear(Tuple<Tuple<List<Token>>> a, Tuple<Tuple<List<Token>>> b, IWitness witness) {
    return witness.isNear(Iterables.getLast(a.right.right), Iterables.get(b.right.right, 0));
  }

}
