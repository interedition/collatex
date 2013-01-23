package eu.interedition.collatex.medite;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.Match;
import eu.interedition.collatex.dekker.Tuple;

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
    final Matcher matcher = Matcher.create(comparator, graph, tokens);

    final IndexRangeSet rankFilter = new IndexRangeSet();
    final IndexRangeSet tokenFilter = new IndexRangeSet();

    final SortedSet<Phrase<TokenMatch>> phraseMatches = Sets.newTreeSet();
    while (true) {
      final SortedSet<Phrase<TokenMatch>> maximalUniqueMatches = matcher.maximalUniqueMatches(rankFilter, tokenFilter);
      if (maximalUniqueMatches.isEmpty()) {
        break;
      }
      phraseMatches.addAll(Aligner.align(maximalUniqueMatches));
      break;
    }

    if (LOG.isLoggable(Level.FINER)) {
      for (Phrase<TokenMatch> phrase : phraseMatches) {
        LOG.log(Level.FINER, "{0} + {1}: Phrase match: {2}", new Object[] { graph, witness, toString(phrase, tokens) });
      }
    }


    final List<Phrase<TokenMatch>> transpositions = transpositions(phraseMatches);
    if (LOG.isLoggable(Level.FINER)) {
      for (Phrase<TokenMatch> transposition : transpositions) {
        LOG.log(Level.FINER, "{0} + {1}: Transposition: {2}", new Object[] { graph, witness, toString(transposition, tokens) });
      }
    }

    final Map<Token, VariantGraph.Vertex> tokenMatches = Maps.newHashMap();
    for (Phrase<TokenMatch> phraseMatch : phraseMatches) {
      for (TokenMatch tokenMatch : phraseMatch) {
        tokenMatches.put(tokens[tokenMatch.token], tokenMatch.vertex);
      }
    }

    final List<List<Match>> transpositionMatches = Lists.newLinkedList();
    for (Phrase<TokenMatch> transposition : transpositions) {
      final List<Match> transpositionMatch = Lists.newLinkedList();
      for (TokenMatch match : transposition) {
        tokenMatches.remove(tokens[match.token]);
        transpositionMatch.add(new Match(match.vertex, tokens[match.token]));
      }
      transpositionMatches.add(transpositionMatch);
    }

    merge(graph, witness, tokenMatches);
    mergeTranspositions(graph, transpositionMatches);
  }

  List<Phrase<TokenMatch>> transpositions(SortedSet<Phrase<TokenMatch>> phraseMatches) {

    // gather matched tokens into a list ordered by their natural order
    final List<Integer> sortedMatchedTokens = Lists.newArrayList();
    for (Phrase<TokenMatch> phraseMatch : phraseMatches) {
      sortedMatchedTokens.add(phraseMatch.first().token);
    }
    Collections.sort(sortedMatchedTokens);

    // detect transpositions
    final List<Phrase<TokenMatch>> transpositions = Lists.newArrayList();

    int previousToken = 0;
    Tuple<Integer> previous = new Tuple<Integer>(0, 0);

    for (Phrase<TokenMatch> phraseMatch : phraseMatches) {
      int currentToken = phraseMatch.first().token;
      int expectedToken = sortedMatchedTokens.get(previousToken);
      Tuple<Integer> current = new Tuple<Integer>(expectedToken, currentToken);
      if (expectedToken != currentToken && !isMirrored(previous, current)) {
        transpositions.add(phraseMatch);
      }
      previousToken++;
      previous = current;
    }
    if (LOG.isLoggable(Level.FINER)) {
      for (Phrase<TokenMatch> transposition : transpositions) {
        LOG.log(Level.FINER, "Detected transposition: {0}", Iterables.toString(transposition));
      }
    }
    return transpositions;
  }

  private boolean isMirrored(Tuple<Integer> previousTuple, Tuple<Integer> tuple) {
    return previousTuple.left.equals(tuple.right) && previousTuple.right.equals(tuple.left);
  }

  String toString(Phrase<TokenMatch> phrase, Token[] tokens) {
    final List<VariantGraph.Vertex> phraseVertices = Lists.newArrayListWithExpectedSize(phrase.size());
    final List<Token> phraseTokens = Lists.newArrayListWithExpectedSize(phrase.size());
    for (TokenMatch tokenMatch : phrase) {
      phraseVertices.add(tokenMatch.vertex);
      phraseTokens.add(tokens[tokenMatch.token]);
    }
   return Iterables.toString(phraseVertices) + " == " + Iterables.toString(phraseTokens);
  }
}
