package eu.interedition.collatex.dekker;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphVertex;

import java.util.*;

public class DekkerAlgorithm extends CollationAlgorithm.Base {
  private final Comparator<Token> comparator;
  private final TokenLinker tokenLinker;
  private final PhraseMatchDetector phraseMatchDetector;
  private final TranspositionDetector transpositionDetector;

  private Map<Token, VariantGraphVertex> tokenLinks;
  private List<List<Match>> phraseMatches;
  private List<List<Match>> transpositions;
  private Map<Token, VariantGraphVertex> alignments;

  public DekkerAlgorithm(Comparator<Token> comparator) {
    this(comparator, new DefaultTokenLinker());
  }

  public DekkerAlgorithm(Comparator<Token> comparator, TokenLinker tokenLinker) {
    this.comparator = comparator;
    this.tokenLinker = tokenLinker;
    this.phraseMatchDetector = new PhraseMatchDetector();
    this.transpositionDetector = new TranspositionDetector();
  }

  @Override
  public void collate(VariantGraph graph, Iterable<Token> tokens) {
    Preconditions.checkArgument(!Iterables.isEmpty(tokens), "Empty witness");
    final Witness witness = Iterables.getFirst(tokens, null).getWitness();

    if (LOG.isTraceEnabled()) {
      LOG.trace("{} + {}: {} vs. {}", new Object[] { graph, witness, graph.vertices(), tokens});
    }

    LOG.debug("{} + {}: Match and link tokens", graph, witness);
    tokenLinks = tokenLinker.link(graph, tokens, comparator);
    if (LOG.isTraceEnabled()) {
      for (Map.Entry<Token, VariantGraphVertex> tokenLink : tokenLinks.entrySet()) {
        LOG.trace("{} + {}: Token match: {} = {}", new Object[] { graph, witness, tokenLink.getValue(), tokenLink.getKey() });
      }
    }

    LOG.debug("{} + {}: Detect phrase matches", graph, witness);
    phraseMatches = phraseMatchDetector.detect(tokenLinks, graph, tokens);
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
    alignments = Maps.newHashMap(tokenLinks);

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

    final Map<Token, VariantGraphVertex> transposedTokens = Maps.newHashMap();
    for (List<Match> transposedPhrase : transpositions) {
      for (Match match : transposedPhrase) {
        transposedTokens.put(match.token, match.vertex);
      }
    }
    
    merge(graph, tokens, alignments, transposedTokens);
    
    if (LOG.isTraceEnabled()) {
      LOG.trace("{}: {}", graph, Iterables.toString(graph.vertices()));
    }
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

  // NOTE: this method should not return the original sequence when a mirror exists!
  private List<List<Match>> filterMirrored(List<Tuple<List<Match>>> transpositions, Witness witness) {
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
  private boolean transpositionsAreNear(Tuple<List<Match>> a, Tuple<List<Match>> b, Witness witness) {
    return witness.isNear(Iterables.getLast(a.right).token, Iterables.get(b.right, 0).token);
  }

}
