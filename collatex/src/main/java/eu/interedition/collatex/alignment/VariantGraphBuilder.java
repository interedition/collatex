package eu.interedition.collatex.alignment;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import eu.interedition.collatex.ITokenLinker;
import eu.interedition.collatex.IWitness;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.Tuple;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphVertex;
import eu.interedition.collatex.matching.EqualityTokenComparator;
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

  private Map<Token, VariantGraphVertex> tokenLinks;
  private List<List<Match>> phraseMatches;
  private List<List<Match>> transpositions;
  private LinkedHashMap<Token, VariantGraphVertex> alignments;

  public VariantGraphBuilder(VariantGraph graph) {
    this(graph, new EqualityTokenComparator(), new DefaultTokenLinker(), new PhraseMatchDetector(), new TranspositionDetector());
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

  public Map<Token, VariantGraphVertex> getTokenLinks() {
    return tokenLinks;
  }

  public List<List<Match>> getPhraseMatches() {
    return Collections.unmodifiableList(phraseMatches);
  }

  public List<List<Match>> getTranspositions() {
    return Collections.unmodifiableList(transpositions);
  }

  public Map<Token, VariantGraphVertex> getAlignments() {
    return Collections.unmodifiableMap(alignments);
  }

  protected void merge(IWitness witness) {
    if (LOG.isTraceEnabled()) {
      LOG.trace("{} + {}: {} vs. {}", new Object[] { graph, witness, graph.vertices(), witness.getTokens() });
    }

    LOG.debug("{} + {}: Match and link tokens", graph, witness);
    tokenLinks = tokenLinker.link(graph, witness.getTokens(), comparator);
    if (LOG.isTraceEnabled()) {
      for (Map.Entry<Token, VariantGraphVertex> tokenLink : tokenLinks.entrySet()) {
        LOG.trace("{} + {}: Token match: {} = {}", new Object[] { graph, witness, tokenLink.getValue(), tokenLink.getKey() });
      }
    }

    LOG.debug("{} + {}: Detect phrase matches", graph, witness);
    phraseMatches = phraseMatchDetector.detect(tokenLinks, graph, witness);
    if (LOG.isTraceEnabled()) {
      for (List<Match> phraseMatch : phraseMatches) {
        LOG.trace("{} + {}: Phrase match: {}", new Object[] { graph, witness, Iterables.toString(phraseMatch) });
      }
    }

    LOG.debug("{} + {}: Detect transpositions", graph, witness);
    transpositions = filterMirrored(transpositionDetector.detect(phraseMatches, graph), witness);
    if (LOG.isTraceEnabled()) {
      for (List<Match> transposition : transpositions) {
        LOG.trace("{} + {}: Transposition: {}", new Object[] { graph, witness, Iterables.toString(transposition) });
      }
    }

    LOG.debug("{} + {}: Determine aligned tokens by filtering transpositions", graph, witness);
    alignments = Maps.newLinkedHashMap(tokenLinks);
    for (List<Match> transposedPhrase : transpositions) {
      for (Match match : transposedPhrase) {
        alignments.remove(match.token);
      }
    }
    if (LOG.isTraceEnabled()) {
      for (Map.Entry<Token, VariantGraphVertex> alignment : alignments.entrySet()) {
        LOG.trace("{} + {}: Alignment: {} = {}", new Object[] { graph, witness, alignment.getValue(), alignment.getKey() });
      }
    }

    LOG.debug("{} + {}: Merge comparand into graph", graph, witness);
    VariantGraphVertex last = graph.getStart();
    final SortedSet<IWitness> witnessSet = Sets.newTreeSet(Collections.singleton(witness));
    final Map<Token, VariantGraphVertex> witnessTokenVertices = Maps.newHashMap();
    for (Token token : witness.getTokens()) {
      VariantGraphVertex matchingVertex = alignments.get(token);
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
    for (List<Match> transposedPhrase : transpositions) {
      for (Match match : transposedPhrase) {
        graph.transpose(match.vertex, witnessTokenVertices.get(match.token));
      }
    }

    if (LOG.isTraceEnabled()) {
      LOG.trace("{}: {}", graph, Iterables.toString(graph.vertices()));
    }
  }

  // NOTE: this method should not return the original sequence when a mirror exists!
  private List<List<Match>> filterMirrored(List<Tuple<List<Match>>> transpositions, IWitness witness) {
    final List<List<Match>> transposed = Lists.newArrayList();
    final Deque<Tuple<List<Match>>> toCheck = new ArrayDeque<Tuple<List<Match>>>(transpositions);
    while (!toCheck.isEmpty()) {
      final Tuple<List<Match>> current = toCheck.pop();
      final Tuple<List<Match>> mirrored = findMirroredTransposition(toCheck, current);
      if (mirrored != null && transpositionsAreNear(current, mirrored, witness)) {
        toCheck.remove(mirrored);
        transposed.add(mirrored.left);
      } else {
        transposed.add(current.left);
      }
    }
    return transposed;
  }

  private Tuple<List<Match>> findMirroredTransposition(final Deque<Tuple<List<Match>>> transToCheck, final Tuple<List<Match>> original) {
    for (final Tuple<List<Match>> transposition : transToCheck) {
      if (transposition.left.equals(original.right)) {
        if (transposition.right.equals(original.left)) {
          return transposition;
        }
      }
    }
    return null;
  }

  // Note: this only calculates the distance between the tokens in the witness.
  // Note: it does not take into account a possible distance in the vertices in the graph!
  private boolean transpositionsAreNear(Tuple<List<Match>> a, Tuple<List<Match>> b, IWitness witness) {
    return witness.isNear(Iterables.getLast(a.right).token, Iterables.get(b.right, 0).token);
  }

}
