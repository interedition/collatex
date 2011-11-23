package eu.interedition.collatex2.implementation.vg_alignment;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.*;
import eu.interedition.collatex2.implementation.containers.witness.WitnessToken;
import eu.interedition.collatex2.implementation.input.NullToken;
import eu.interedition.collatex2.implementation.matching.EqualityTokenComparator;
import eu.interedition.collatex2.implementation.matching.Match;
import eu.interedition.collatex2.implementation.matching.Matches;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.ITokenLinker;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.google.common.collect.Lists.reverse;

public class TokenLinker implements ITokenLinker {
  private static final Logger LOG = LoggerFactory.getLogger(TokenLinker.class);

  @Override
  public Map<INormalizedToken, INormalizedToken> link(IVariantGraph graph, IWitness b) {
    final IWitness a = new Superbase(graph);

    LOG.trace("Matching tokens of {} and {}", a, b);
    final Matches matches = Matches.between(a, b, new EqualityTokenComparator());

    // add start and end tokens as matches
    Multimap<INormalizedToken, INormalizedToken> boundedMatches = ArrayListMultimap.create(matches.getAll());
    boundedMatches.put(WitnessToken.START, a.getTokens().get(0));
    boundedMatches.put(WitnessToken.END, a.getTokens().get(a.size() - 1));

    LOG.trace("Finding minimal unique token sequences");
    final List<INormalizedToken> bTokens = b.getTokens();
    final int bTokenCount = bTokens.size();

    final List<List<INormalizedToken>> leftExpandingPhrases =  Lists.newArrayListWithExpectedSize(matches.getAmbiguous().size());
    final List<List<INormalizedToken>> rightExpandingPhrases =  Lists.newArrayListWithExpectedSize(matches.getAmbiguous().size());

    for (int tc = 0; tc < bTokenCount; tc++) {
      // for each ambiguous token
      if (matches.getAmbiguous().contains(bTokens.get(tc))) {
        // find a minimal unique phrase by walking to the left
        rightExpandingPhrases.add(reverse(findMinimalUniquePrefix(reverse(bTokens.subList(0, tc + 1)), matches.getUnmatched(), matches.getAmbiguous(), WitnessToken.START)));
        // find a minimal unique phrase by walking to the right
        leftExpandingPhrases.add(findMinimalUniquePrefix(bTokens.subList(tc, bTokenCount), matches.getUnmatched(), matches.getAmbiguous(), WitnessToken.END));
      }
    }

    LOG.trace("Find matches from the base");
    List<INormalizedToken> aMatches = findMatches(a, boundedMatches.values());

    // try and find matches in the base for each sequence in the witness
    List<Match<List<INormalizedToken>>> phraseMatches = Lists.newArrayList();
    for (List<INormalizedToken> phrase : rightExpandingPhrases) {
      List<INormalizedToken> matchingPhrase = findMatchingPhraseToTheRight(phrase, boundedMatches, aMatches);
      if (!matchingPhrase.isEmpty()) {
        phraseMatches.add(new Match<List<INormalizedToken>>(matchingPhrase, phrase));
      }
    }
    for (List<INormalizedToken> phrase : leftExpandingPhrases) {
      List<INormalizedToken> matchingPhrase = findMatchingPhraseToTheLeft(phrase, boundedMatches, aMatches);
      if (!matchingPhrase.isEmpty()) {
        phraseMatches.add(new Match<List<INormalizedToken>>(matchingPhrase, phrase));
      }
    }

    // run the old filter method
    phraseMatches = filterAwaySecondChoicesMultipleColumnsOneToken(Collections.unmodifiableList(phraseMatches));
    phraseMatches = filterAwaySecondChoicesMultipleTokensOneColumn(Collections.unmodifiableList(phraseMatches));

    // do the matching
    final Multimap<INormalizedToken, INormalizedToken> allMatches = matches.getAll();

    final Map<INormalizedToken, INormalizedToken> tokenLinks = Maps.newLinkedHashMap();

    for (INormalizedToken unique : matches.getUnique()) {
      // put unique matches in the result
      tokenLinks.put(unique, Iterables.getFirst(allMatches.get(unique), null));
    }
    // add matched sequences to the result
    for (Match<List<INormalizedToken>> phraseMatch : phraseMatches) {
      final Iterator<INormalizedToken> baseIt = phraseMatch.left.iterator();
      final Iterator<INormalizedToken> witnessIt = phraseMatch.right.iterator();
      while (baseIt.hasNext() && witnessIt.hasNext()) {
        final INormalizedToken baseToken = baseIt.next();
        final INormalizedToken witnessToken = witnessIt.next();
        if (WitnessToken.START.equals(witnessToken) || WitnessToken.END.equals(witnessToken)) {
          // skip start and end tokens
          continue;
        }
        tokenLinks.put(witnessToken, baseToken);
      }
    }
    return tokenLinks;
  }

  @Deprecated
  public static List<List<INormalizedToken>> findUniqueTokenSequences(IWitness witness, Matches matches) {
    final List<INormalizedToken> tokens = witness.getTokens();
    final int tokenCount = tokens.size();

    final List<List<INormalizedToken>> tokenSequences =  Lists.newArrayListWithExpectedSize(matches.getAmbiguous().size() * 2);

    for (int tc = 0; tc < tokenCount; tc++) {
      // for each ambiguous token
      if (matches.getAmbiguous().contains(tokens.get(tc))) {
        // find a minimal unique subsequence by walking to the left
        tokenSequences.add(reverse(findMinimalUniquePrefix(reverse(tokens.subList(0, tc + 1)), matches.getUnmatched(), matches.getAmbiguous(), WitnessToken.START)));
        // find a minimal unique subsequence by walking to the right
        tokenSequences.add(findMinimalUniquePrefix(tokens.subList(tc, tokenCount), matches.getUnmatched(), matches.getAmbiguous(), WitnessToken.END));
      }
    }

    return tokenSequences;
  }

  public static List<INormalizedToken> findMinimalUniquePrefix(Iterable<INormalizedToken> sequence, Set<INormalizedToken> unmatched, Set<INormalizedToken> ambiguous, INormalizedToken stopMarker) {
    final List<INormalizedToken> result = Lists.newArrayList();

    for (INormalizedToken token : sequence) {
      if (!unmatched.contains(token)) {
        result.add(token);
        if (!ambiguous.contains(token)) {
          return result;
        }
      }
    }

    result.add(stopMarker);
    return result;
  }

  // check whether this match has an alternative that is equal in weight
  // if so, then skip the alternative!
  // NOTE: multiple witness tokens match with the same table column!
  //EXPECT GRAPH -> Witness TOKEN HERE
  private List<Match<List<INormalizedToken>>> filterAwaySecondChoicesMultipleTokensOneColumn(List<Match<List<INormalizedToken>>> sequences) {
    List<Match<List<INormalizedToken>>> filteredMatches = Lists.newArrayList();
    final Map<INormalizedToken, INormalizedToken> tableToToken = Maps.newLinkedHashMap();
    for (final Match<List<INormalizedToken>> sequence : sequences) {
      // step 1. Gather data
      List<TokenPair> pairs = Lists.newArrayList();
      final List<INormalizedToken> tablePhrase = sequence.left;
      final List<INormalizedToken> witnessPhrase = sequence.right;
      final Iterator<INormalizedToken> tokens = witnessPhrase.iterator();
      for (final INormalizedToken tableToken : tablePhrase) {
        final INormalizedToken token = tokens.next();
        // skip Start and End Token in variant graph... string equals is not very nice!
        if (!(tableToken.getNormalized().equals("#"))) {
          pairs.add(new TokenPair(tableToken, token));
        }
      }
      // step 2. Look for alternative
      boolean foundAlternative = false;
      for (TokenPair pair : pairs) {
        // check for alternative here!
        final INormalizedToken tableToken = pair.tableToken;
        final INormalizedToken witnessToken = pair.witnessToken;
        if (tableToToken.containsKey(tableToken)) {
          INormalizedToken existingWitnessToken = tableToToken.get(tableToken);
          if (existingWitnessToken != witnessToken) {
            foundAlternative = true;
          }
        } else {
          tableToToken.put(tableToken, witnessToken);
        }
      }
      // step 3. Decide what to do
      if (!foundAlternative) {
        filteredMatches.add(sequence);
      } else {
//        LOG.debug("!Phrase '" + witnessPhrase + "' is an alternative! skipping...");
      }
    }
    return filteredMatches;
  }


  // check whether this match has an alternative that is equal in weight
  // if so, then skip the alternative!
  // NOTE: multiple columns match with the same token!
  // EXPECT WITNESS TOKEN TO GRAPH TOKEN HERE!
  private List<Match<List<INormalizedToken>>> filterAwaySecondChoicesMultipleColumnsOneToken(List<Match<List<INormalizedToken>>> sequences) {
    List<Match<List<INormalizedToken>>> filteredMatches = Lists.newArrayList();
    final Map<INormalizedToken, INormalizedToken> tokenToTable = Maps.newLinkedHashMap();
    for (final Match<List<INormalizedToken>> sequence : sequences) {
      // step 1. Gather data
      List<TokenPair> pairs = Lists.newArrayList();
      final List<INormalizedToken> tablePhrase = sequence.left;
      final List<INormalizedToken> witnessPhrase = sequence.right;
      final Iterator<INormalizedToken> tokens = witnessPhrase.iterator();
      for (final INormalizedToken tableToken : tablePhrase) {
        final INormalizedToken token = tokens.next();
        // skip NullColumn and NullToken
        if (!(tableToken instanceof NullToken)) {
          pairs.add(new TokenPair(tableToken, token));
        }
      }
      // step 2. Look for alternative
      boolean foundAlternative = false;
      for (TokenPair pair : pairs) {
        // check for alternative here!
        final INormalizedToken tableToken = pair.tableToken;
        final INormalizedToken token = pair.witnessToken;
        if (tokenToTable.containsKey(token)) {
          INormalizedToken existingTable = tokenToTable.get(token);
          if (existingTable != tableToken) {
            foundAlternative = true;
          }
        } else {
          tokenToTable.put(token, tableToken);
        }
      }
      // step 3. Decide what to do
      if (!foundAlternative) {
        filteredMatches.add(sequence);
      } else {
//        LOG.debug("Phrase '" + witnessPhrase + "' is an alternative! skipping...");
      }
    }
    return filteredMatches;
  }

  // This method should return the matching base tokens for a given sequence
  // Note: this method works for sequences that have the fixed token on the left and expand to the right
  private List<INormalizedToken> findMatchingPhraseToTheRight(List<INormalizedToken> sequence, Multimap<INormalizedToken, INormalizedToken> matches, List<INormalizedToken> aMatches) {
    final INormalizedToken startTokenInB = sequence.get(0);
    final Collection<INormalizedToken> startMatches = matches.get(startTokenInB);

    Preconditions.checkState(!startMatches.isEmpty(), "No match found in base for fixed witness token");
    Preconditions.checkState(startMatches.size() == 1, "Multiple matches found in base for fixed witness token");

    final INormalizedToken startTokenInA = Iterables.getFirst(startMatches, null);
    return tryTheDifferentPossibilities(matches, aMatches, startTokenInA, sequence.subList(1, sequence.size()), 1);
  }

  // This method should return the matching base tokens for a given sequence
  // Note: this method works for sequences that have the fixed token on the right and expand to the left
  private List<INormalizedToken> findMatchingPhraseToTheLeft(List<INormalizedToken> sequence, Multimap<INormalizedToken, INormalizedToken> matches, List<INormalizedToken> aMatches) {
    final INormalizedToken startTokenInB = sequence.get(sequence.size() - 1);
    final Collection<INormalizedToken> startMatches = matches.get(startTokenInB);

    Preconditions.checkState(!startMatches.isEmpty(), "No match found in base for fixed witness token");
    Preconditions.checkState(startMatches.size() == 1, "Multiple matches found in base for fixed witness token");

    INormalizedToken startTokenInA = Iterables.getFirst(startMatches, null);
    return reverse(tryTheDifferentPossibilities(matches, aMatches, startTokenInA, reverse(sequence.subList(0, sequence.size() - 1)), -1));
  }

  private List<INormalizedToken> tryTheDifferentPossibilities(Multimap<INormalizedToken, INormalizedToken> matches, List<INormalizedToken> aMatches, INormalizedToken startTokenInA,
      List<INormalizedToken> restTokens, int expectedDirection) {
    boolean validWholeSequence = true;
    List<INormalizedToken> matchedBaseTokens = Lists.newArrayList(startTokenInA);
    INormalizedToken lastToken = startTokenInA;
    for (INormalizedToken token : restTokens) {
      boolean valid = false;
      for (INormalizedToken possibleMatch : matches.get(token)) {
        int direction = aMatches.indexOf(possibleMatch) - aMatches.indexOf(lastToken);
        if (direction == expectedDirection) {
          matchedBaseTokens.add(possibleMatch);
          lastToken = possibleMatch;
          valid = true;
          break;
        }
      }
      validWholeSequence = valid;
      if (!valid) {
        break;
      }
    }
    if (validWholeSequence) {
      return matchedBaseTokens;
    }
    return Collections.emptyList();
  }

  public static List<INormalizedToken> findMatches(IWitness a, Collection<INormalizedToken> matches) {
    return Lists.newArrayList(Iterables.filter(a.getTokens(), Predicates.in(matches)));
  }

  public static String toString(List<INormalizedToken> tokens) {
    final StringBuilder str = new StringBuilder();
    for (INormalizedToken token : tokens) {
      str.append(token.getNormalized()).append(" ");
    }
    return str.toString().trim();

  }
}
