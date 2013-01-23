package eu.interedition.collatex.medite;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.Match;
import eu.interedition.collatex.dekker.Tuple;
import eu.interedition.collatex.util.VariantGraphRanking;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.logging.Level;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class MediteAlgorithm extends CollationAlgorithm.Base {


  private final Comparator<Token> comparator;

  public MediteAlgorithm(Comparator<Token> comparator) {
    this.comparator = comparator;
  }

  @Override
  public void collate(VariantGraph graph, Iterable<Token> witness) {
    final Token[] tokens = Iterables.toArray(witness, Token.class);

    final Stopwatch stopwatch = new Stopwatch();

    if (LOG.isLoggable(Level.FINE)) {
      stopwatch.start();
    }

    final SuffixTree<Token> suffixTree = SuffixTree.build(comparator, tokens);

    if (LOG.isLoggable(Level.FINE)) {
      stopwatch.stop();
      LOG.log(Level.FINE, "Built suffix tree of {0} token(s) in {1}", new Object[] { tokens.length, stopwatch });
      stopwatch.reset().start();
    }

    final VariantGraphRanking ranking = VariantGraphRanking.of(graph);

    if (LOG.isLoggable(Level.FINE)) {
      stopwatch.stop();
      LOG.log(Level.FINE, "Ranked variant graph of {0} in {1}", new Object[] { graph, stopwatch });
      stopwatch.reset().start();
    }


    final Matches matcher = Matches.between(comparator, ranking, suffixTree);

    if (LOG.isLoggable(Level.FINE)) {
      stopwatch.stop();
      LOG.log(Level.FINE, "Found {0} matching phrase(s) in {1}", new Object[] { matcher.size(), stopwatch });
      stopwatch.reset().start();
    }

    final IndexRangeSet rankFilter = new IndexRangeSet();
    final IndexRangeSet tokenFilter = new IndexRangeSet();

    final SortedSet<Phrase<eu.interedition.collatex.medite.Match.WithTokenIndex>> phraseMatches = Sets.newTreeSet();
    while (true) {
      final SortedSet<Phrase<eu.interedition.collatex.medite.Match.WithTokenIndex>> maximalUniqueMatches = matcher.removeMaximalUniqueMatches(rankFilter, tokenFilter);
      if (maximalUniqueMatches.isEmpty()) {
        break;
      }
      phraseMatches.addAll(Aligner.align(maximalUniqueMatches));
      break;
    }

    if (LOG.isLoggable(Level.FINE)) {
      stopwatch.stop();
      LOG.log(Level.FINE, "Selected {0} maximal unique matches in {1}", new Object[] { phraseMatches.size(), stopwatch });
      stopwatch.reset().start();
    }

    final List<Phrase<eu.interedition.collatex.medite.Match.WithTokenIndex>> transpositions = transpositions(phraseMatches);

    if (LOG.isLoggable(Level.FINE)) {
      stopwatch.stop();
      LOG.log(Level.FINE, "Detected {0} transpositions in {1} maximal unique matches in {2}", new Object[] { transpositions.size(), phraseMatches.size(), stopwatch });
      stopwatch.reset().start();
    }

    final Map<Token, VariantGraph.Vertex> tokenMatches = Maps.newHashMap();
    for (Phrase<eu.interedition.collatex.medite.Match.WithTokenIndex> phraseMatch : phraseMatches) {
      for (eu.interedition.collatex.medite.Match.WithTokenIndex tokenMatch : phraseMatch) {
        tokenMatches.put(tokens[tokenMatch.token], tokenMatch.vertex);
      }
    }

    final List<List<Match>> transpositionMatches = Lists.newLinkedList();
    for (Phrase<eu.interedition.collatex.medite.Match.WithTokenIndex> transposition : transpositions) {
      final List<Match> transpositionMatch = Lists.newLinkedList();
      for (eu.interedition.collatex.medite.Match.WithTokenIndex match : transposition) {
        tokenMatches.remove(tokens[match.token]);
        transpositionMatch.add(new Match(match.vertex, tokens[match.token]));
      }
      transpositionMatches.add(transpositionMatch);
    }

    merge(graph, witness, tokenMatches);
    mergeTranspositions(graph, transpositionMatches);

    if (LOG.isLoggable(Level.FINE)) {
      stopwatch.stop();
      LOG.log(Level.FINE, "Merged {0} token matches and {1} transpositions in {2}", new Object[] { tokenMatches.size(), transpositions.size(), stopwatch });
    }
  }

  List<Phrase<eu.interedition.collatex.medite.Match.WithTokenIndex>> transpositions(SortedSet<Phrase<eu.interedition.collatex.medite.Match.WithTokenIndex>> phraseMatches) {

    // gather matched tokens into a list ordered by their natural order
    final List<Integer> sortedMatchedTokens = Lists.newArrayList();
    for (Phrase<eu.interedition.collatex.medite.Match.WithTokenIndex> phraseMatch : phraseMatches) {
      sortedMatchedTokens.add(phraseMatch.first().token);
    }
    Collections.sort(sortedMatchedTokens);

    // detect transpositions
    final List<Phrase<eu.interedition.collatex.medite.Match.WithTokenIndex>> transpositions = Lists.newArrayList();

    int previousToken = 0;
    Tuple<Integer> previous = new Tuple<Integer>(0, 0);

    for (Phrase<eu.interedition.collatex.medite.Match.WithTokenIndex> phraseMatch : phraseMatches) {
      int currentToken = phraseMatch.first().token;
      int expectedToken = sortedMatchedTokens.get(previousToken);
      Tuple<Integer> current = new Tuple<Integer>(expectedToken, currentToken);
      if (expectedToken != currentToken && !isMirrored(previous, current)) {
        transpositions.add(phraseMatch);
      }
      previousToken++;
      previous = current;
    }
    return transpositions;
  }

  private boolean isMirrored(Tuple<Integer> previousTuple, Tuple<Integer> tuple) {
    return previousTuple.left.equals(tuple.right) && previousTuple.right.equals(tuple.left);
  }

  String toString(Phrase<eu.interedition.collatex.medite.Match.WithTokenIndex> phrase, Token[] tokens) {
    final List<VariantGraph.Vertex> phraseVertices = Lists.newArrayListWithExpectedSize(phrase.size());
    final List<Token> phraseTokens = Lists.newArrayListWithExpectedSize(phrase.size());
    for (eu.interedition.collatex.medite.Match.WithTokenIndex tokenMatch : phrase) {
      phraseVertices.add(tokenMatch.vertex);
      phraseTokens.add(tokens[tokenMatch.token]);
    }
   return Iterables.toString(phraseVertices) + " == " + Iterables.toString(phraseTokens);
  }
}
