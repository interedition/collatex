package eu.interedition.collatex2.implementation.alignment;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.*;
import eu.interedition.collatex2.implementation.Tuple;
import eu.interedition.collatex2.implementation.input.NormalizedToken;
import eu.interedition.collatex2.implementation.matching.Matches;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.ITokenLinker;
import eu.interedition.collatex2.interfaces.IWitness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.google.common.collect.Lists.reverse;

public class TokenLinker implements ITokenLinker {
  private static final Logger LOG = LoggerFactory.getLogger(TokenLinker.class);

  private Matches matches;
  private List<List<INormalizedToken>> leftExpandingPhrases;
  private List<List<INormalizedToken>> rightExpandingPhrases;
  private List<Tuple<List<INormalizedToken>>> phraseMatches;
  private Map<INormalizedToken,INormalizedToken> tokenLinks;

  @Override
  public Map<INormalizedToken, INormalizedToken> link(IWitness a, IWitness b, Comparator<INormalizedToken> comparator) {
    LOG.trace("Matching tokens of {} and {}", a, b);
    matches = Matches.between(a, b, comparator);

    // add start and end tokens as matches
    final Multimap<INormalizedToken, INormalizedToken> boundedMatches = ArrayListMultimap.create(matches.getAll());
    boundedMatches.put(NormalizedToken.START, a.getTokens().get(0));
    boundedMatches.put(NormalizedToken.END, a.getTokens().get(a.size() - 1));

    LOG.trace("Finding minimal unique token sequences");
    final List<INormalizedToken> bTokens = b.getTokens();
    final int bTokenCount = bTokens.size();

    leftExpandingPhrases = Lists.newArrayListWithExpectedSize(matches.getAmbiguous().size());
    rightExpandingPhrases = Lists.newArrayListWithExpectedSize(matches.getAmbiguous().size());

    for (int tc = 0; tc < bTokenCount; tc++) {
      // for each ambiguous token
      if (matches.getAmbiguous().contains(bTokens.get(tc))) {
        // find a minimal unique phrase by walking to the left
        rightExpandingPhrases.add(reverse(findMinimalUniquePrefix(reverse(bTokens.subList(0, tc + 1)), matches.getUnmatched(), matches.getAmbiguous(), NormalizedToken.START)));
        // find a minimal unique phrase by walking to the right
        leftExpandingPhrases.add(findMinimalUniquePrefix(bTokens.subList(tc, bTokenCount), matches.getUnmatched(), matches.getAmbiguous(), NormalizedToken.END));
      }
    }

    LOG.trace("Find matches from the base");
    List<INormalizedToken> aMatches = findMatches(a, boundedMatches.values());

    // try and find matches in the base for each sequence in the witness
    phraseMatches = Lists.newArrayList();
    for (List<INormalizedToken> phrase : rightExpandingPhrases) {
      List<INormalizedToken> matchingPhrase = findMatchingPhraseToTheRight(phrase, boundedMatches, aMatches);
      if (!matchingPhrase.isEmpty()) {
        phraseMatches.add(new Tuple<List<INormalizedToken>>(matchingPhrase, phrase));
      }
    }
    for (List<INormalizedToken> phrase : leftExpandingPhrases) {
      List<INormalizedToken> matchingPhrase = findMatchingPhraseToTheLeft(phrase, boundedMatches, aMatches);
      if (!matchingPhrase.isEmpty()) {
        phraseMatches.add(new Tuple<List<INormalizedToken>>(matchingPhrase, phrase));
      }
    }

    // run the old filter method
    filterSecondaryPhraseMatches(phraseMatches, Functions.<Tuple<INormalizedToken>>identity());
    filterSecondaryPhraseMatches(phraseMatches, Tuple.<INormalizedToken>flipFunction());

    // do the matching
    final Multimap<INormalizedToken, INormalizedToken> allMatches = matches.getAll();

    tokenLinks = Maps.newLinkedHashMap();

    for (INormalizedToken unique : matches.getUnique()) {
      // put unique matches in the result
      tokenLinks.put(unique, Iterables.getFirst(allMatches.get(unique), null));
    }
    // add matched sequences to the result
    for (Tuple<List<INormalizedToken>> phraseMatch : phraseMatches) {
      final Iterator<INormalizedToken> baseIt = phraseMatch.left.iterator();
      final Iterator<INormalizedToken> witnessIt = phraseMatch.right.iterator();
      while (baseIt.hasNext() && witnessIt.hasNext()) {
        final INormalizedToken baseToken = baseIt.next();
        final INormalizedToken witnessToken = witnessIt.next();
        if (NormalizedToken.START.equals(witnessToken) || NormalizedToken.END.equals(witnessToken)) {
          // skip start and end tokens
          continue;
        }
        tokenLinks.put(witnessToken, baseToken);
      }
    }
    return tokenLinks;
  }

  public Matches getMatches() {
    return matches;
  }

  public List<List<INormalizedToken>> getLeftExpandingPhrases() {
    return leftExpandingPhrases;
  }

  public List<List<INormalizedToken>> getRightExpandingPhrases() {
    return rightExpandingPhrases;
  }

  public List<Tuple<List<INormalizedToken>>> getPhraseMatches() {
    return phraseMatches;
  }

  public Map<INormalizedToken, INormalizedToken> getTokenLinks() {
    return tokenLinks;
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

  private void filterSecondaryPhraseMatches(List<Tuple<List<INormalizedToken>>> phraseMatches, Function<Tuple<INormalizedToken>, Tuple<INormalizedToken>> tokenMatchTransform) {
    final Map<INormalizedToken, INormalizedToken> previousMatches = Maps.newLinkedHashMap();
    for (Iterator<Tuple<List<INormalizedToken>>> phraseMatchIt = phraseMatches.iterator(); phraseMatchIt.hasNext(); ) {
      boolean foundAlternative = false;
      final Tuple<List<INormalizedToken>> phraseMatch = phraseMatchIt.next();

      final List<Tuple<INormalizedToken>> tokenMatches = Lists.newArrayList();
      final Iterator<INormalizedToken> leftIt = phraseMatch.left.iterator();
      final Iterator<INormalizedToken> rightIt = phraseMatch.right.iterator();
      while (leftIt.hasNext() && rightIt.hasNext()) {
        final INormalizedToken left = leftIt.next();
        final INormalizedToken right = rightIt.next();
        if (NormalizedToken.START.equals(left) || NormalizedToken.END.equals(left)) {
          // skip start and end Token in variant graph
          continue;
        }
        tokenMatches.add(tokenMatchTransform.apply(new Tuple<INormalizedToken>(left, right)));
      }

      for (Tuple<INormalizedToken> tokenMatch : tokenMatches) {
        final INormalizedToken previousMatch = previousMatches.get(tokenMatch.left);
        if (previousMatch != null && !previousMatch.equals(tokenMatch.right)) {
          foundAlternative = true;
        } else {
          previousMatches.put(tokenMatch.left, tokenMatch.right);
        }
      }
      if (foundAlternative) {
        phraseMatchIt.remove();
      }
    }
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
}
