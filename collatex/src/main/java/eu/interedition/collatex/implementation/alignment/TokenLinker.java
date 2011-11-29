package eu.interedition.collatex.implementation.alignment;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicates;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import eu.interedition.collatex.implementation.Tuple;
import eu.interedition.collatex.implementation.input.NormalizedToken;
import eu.interedition.collatex.implementation.matching.Matches;
import eu.interedition.collatex.interfaces.INormalizedToken;
import eu.interedition.collatex.interfaces.ITokenLinker;
import eu.interedition.collatex.interfaces.IWitness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.reverse;

public class TokenLinker implements ITokenLinker {
  private static final Logger LOG = LoggerFactory.getLogger(TokenLinker.class);

  private Matches matches;
  private List<List<INormalizedToken>> leftExpandingPhrases;
  private List<List<INormalizedToken>> rightExpandingPhrases;
  private List<INormalizedToken> baseMatches;
  private List<Tuple<List<INormalizedToken>>> phraseMatches;
  private Map<INormalizedToken,INormalizedToken> tokenLinks;

  @Override
  public Map<INormalizedToken, INormalizedToken> link(IWitness base, IWitness witness, Comparator<INormalizedToken> comparator) {
    LOG.trace("Matching tokens of {} and {}", base, witness);
    matches = Matches.between(base, witness, comparator);

    // add start and end tokens as matches
    final Multimap<INormalizedToken, INormalizedToken> boundedMatches = ArrayListMultimap.create(matches.getAll());
    boundedMatches.put(NormalizedToken.START, base.getTokens().get(0));
    boundedMatches.put(NormalizedToken.END, base.getTokens().get(base.size() - 1));

    LOG.trace("Finding minimal unique token sequences");
    final List<INormalizedToken> witnessTokens = witness.getTokens();
    final int witnessTokenCount = witnessTokens.size();

    leftExpandingPhrases = Lists.newArrayListWithExpectedSize(matches.getAmbiguous().size());
    rightExpandingPhrases = Lists.newArrayListWithExpectedSize(matches.getAmbiguous().size());

    for (int tc = 0; tc < witnessTokenCount; tc++) {
      // for each ambiguous token
      if (matches.getAmbiguous().contains(witnessTokens.get(tc))) {
        // find a minimal unique phrase by walking to the left
        rightExpandingPhrases.add(reverse(findMinimalUniquePrefix(reverse(witnessTokens.subList(0, tc + 1)), NormalizedToken.START)));
        // find a minimal unique phrase by walking to the right
        leftExpandingPhrases.add(findMinimalUniquePrefix(witnessTokens.subList(tc, witnessTokenCount), NormalizedToken.END));
      }
    }

    LOG.trace("Find matches in the base");
    baseMatches = Lists.newArrayList(Iterables.filter(base.getTokens(), Predicates.in(boundedMatches.values())));

    // try and find matches in the base for each sequence in the witness
    phraseMatches = Lists.newArrayList();
    for (List<INormalizedToken> phrase : rightExpandingPhrases) {
      final List<INormalizedToken> matchingPhrase = matchPhrase(boundedMatches, phrase, 1);
      if (!matchingPhrase.isEmpty()) {
        phraseMatches.add(new Tuple<List<INormalizedToken>>(matchingPhrase, phrase));
      }
    }
    for (List<INormalizedToken> phrase : leftExpandingPhrases) {
      final List<INormalizedToken> matchingPhrase = reverse(matchPhrase(boundedMatches, reverse(phrase), -1));
      if (!matchingPhrase.isEmpty()) {
        phraseMatches.add(new Tuple<List<INormalizedToken>>(matchingPhrase, phrase));
      }
    }

    // run the old filter method
    filterAlternativePhraseMatches(phraseMatches, Functions.<Tuple<INormalizedToken>>identity());
    filterAlternativePhraseMatches(phraseMatches, Tuple.<INormalizedToken>flipFunction());

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

  public List<INormalizedToken> getBaseMatches() {
    return baseMatches;
  }

  public List<Tuple<List<INormalizedToken>>> getPhraseMatches() {
    return phraseMatches;
  }

  public Map<INormalizedToken, INormalizedToken> getTokenLinks() {
    return tokenLinks;
  }

  public List<INormalizedToken> findMinimalUniquePrefix(Iterable<INormalizedToken> phrase, INormalizedToken stopMarker) {
    final List<INormalizedToken> result = Lists.newArrayList();

    for (INormalizedToken token : phrase) {
      if (!matches.getUnmatched().contains(token)) {
        result.add(token);
        if (!matches.getAmbiguous().contains(token)) {
          return result;
        }
      }
    }

    result.add(stopMarker);
    return result;
  }

  private void filterAlternativePhraseMatches(List<Tuple<List<INormalizedToken>>> phraseMatches, Function<Tuple<INormalizedToken>, Tuple<INormalizedToken>> tokenMatchTransform) {
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

  private List<INormalizedToken> matchPhrase(Multimap<INormalizedToken, INormalizedToken> matches, List<INormalizedToken> phrase, int expectedDirection) {
    final List<INormalizedToken> matchedPhrase = Lists.newArrayList();

    INormalizedToken lastMatch = null;
    int lastMatchIndex = 0;
    for (INormalizedToken token : phrase) {
      if (lastMatch == null) {
        lastMatch = Iterables.get(matches.get(token), 0);
        lastMatchIndex = baseMatches.indexOf(lastMatch);
        matchedPhrase.add(lastMatch);
        continue;
      }
      boolean tokenMatched = false;
      for (INormalizedToken match : matches.get(token)) {
        final int matchIndex = baseMatches.indexOf(match);
        int direction = matchIndex - lastMatchIndex;
        if (direction == expectedDirection) {
          lastMatch = match;
          lastMatchIndex = matchIndex;
          matchedPhrase.add(match);
          tokenMatched = true;
          break;
        }
      }
      if (!tokenMatched) {
        return Collections.emptyList();
      }
    }
    return matchedPhrase;
  }

}
