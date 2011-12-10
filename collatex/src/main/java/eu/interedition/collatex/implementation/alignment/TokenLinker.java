package eu.interedition.collatex.implementation.alignment;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import eu.interedition.collatex.implementation.Tuple;
import eu.interedition.collatex.implementation.input.SimpleToken;
import eu.interedition.collatex.implementation.matching.Matches;
import eu.interedition.collatex.interfaces.Token;
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
  private List<List<Token>> leftExpandingPhrases;
  private List<List<Token>> rightExpandingPhrases;
  private List<Token> baseMatches;
  private List<Tuple<List<Token>>> phraseMatches;
  private Map<Token,Token> tokenLinks;

  @Override
  public Map<Token, Token> link(IWitness base, IWitness witness, Comparator<Token> comparator) {
    LOG.trace("Matching tokens of {} and {}", base, witness);
    matches = Matches.between(base, witness, comparator);

    LOG.trace("Finding minimal unique token sequences");
    final List<Token> witnessTokens = witness.getTokens();
    final int witnessTokenCount = witnessTokens.size();

    leftExpandingPhrases = Lists.newArrayListWithExpectedSize(matches.getAmbiguous().size());
    rightExpandingPhrases = Lists.newArrayListWithExpectedSize(matches.getAmbiguous().size());

    for (int tc = 0; tc < witnessTokenCount; tc++) {
      // for each ambiguous token
      if (matches.getAmbiguous().contains(witnessTokens.get(tc))) {
        // find a minimal unique phrase by walking to the left
        rightExpandingPhrases.add(reverse(findMinimalUniquePrefix(reverse(witnessTokens.subList(0, tc + 1)), SimpleToken.START)));
        // find a minimal unique phrase by walking to the right
        leftExpandingPhrases.add(findMinimalUniquePrefix(witnessTokens.subList(tc, witnessTokenCount), SimpleToken.END));
      }
    }

    LOG.trace("Find matches in the base");
    baseMatches = Lists.newArrayList(Iterables.filter(base.getTokens(), Predicates.in(matches.getAll().values())));

    // try and find matches in the base for each sequence in the witness
    phraseMatches = Lists.newArrayList();
    for (List<Token> phrase : rightExpandingPhrases) {
      final List<Token> matchingPhrase = matchPhrase(phrase, 1);
      if (!matchingPhrase.isEmpty()) {
        phraseMatches.add(new Tuple<List<Token>>(matchingPhrase, phrase));
      }
    }
    for (List<Token> phrase : leftExpandingPhrases) {
      final List<Token> matchingPhrase = reverse(matchPhrase(reverse(phrase), -1));
      if (!matchingPhrase.isEmpty()) {
        phraseMatches.add(new Tuple<List<Token>>(matchingPhrase, phrase));
      }
    }

    // run the old filter method
    filterAlternativePhraseMatches(phraseMatches, Functions.<Tuple<Token>>identity());
    filterAlternativePhraseMatches(phraseMatches, Tuple.<Token>flipFunction());

    // do the matching
    final Multimap<Token, Token> allMatches = matches.getAll();

    tokenLinks = Maps.newLinkedHashMap();

    for (Token unique : matches.getUnique()) {
      // put unique matches in the result
      tokenLinks.put(unique, Iterables.getFirst(allMatches.get(unique), null));
    }
    // add matched sequences to the result
    for (Tuple<List<Token>> phraseMatch : phraseMatches) {
      final Iterator<Token> baseIt = phraseMatch.left.iterator();
      final Iterator<Token> witnessIt = phraseMatch.right.iterator();
      while (baseIt.hasNext() && witnessIt.hasNext()) {
        final Token baseToken = baseIt.next();
        final Token witnessToken = witnessIt.next();
        if (SimpleToken.START.equals(witnessToken) || SimpleToken.END.equals(witnessToken)) {
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

  public List<List<Token>> getLeftExpandingPhrases() {
    return leftExpandingPhrases;
  }

  public List<List<Token>> getRightExpandingPhrases() {
    return rightExpandingPhrases;
  }

  public List<Token> getBaseMatches() {
    return baseMatches;
  }

  public List<Tuple<List<Token>>> getPhraseMatches() {
    return phraseMatches;
  }

  public Map<Token, Token> getTokenLinks() {
    return tokenLinks;
  }

  public List<Token> findMinimalUniquePrefix(Iterable<Token> phrase, Token stopMarker) {
    final List<Token> result = Lists.newArrayList();

    for (Token token : phrase) {
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

  private void filterAlternativePhraseMatches(List<Tuple<List<Token>>> phraseMatches, Function<Tuple<Token>, Tuple<Token>> tokenMatchTransform) {
    final Map<Token, Token> previousMatches = Maps.newLinkedHashMap();
    for (Iterator<Tuple<List<Token>>> phraseMatchIt = phraseMatches.iterator(); phraseMatchIt.hasNext(); ) {
      boolean foundAlternative = false;
      final Tuple<List<Token>> phraseMatch = phraseMatchIt.next();

      final List<Tuple<Token>> tokenMatches = Lists.newArrayList();
      final Iterator<Token> leftIt = phraseMatch.left.iterator();
      final Iterator<Token> rightIt = phraseMatch.right.iterator();
      while (leftIt.hasNext() && rightIt.hasNext()) {
        final Token left = leftIt.next();
        final Token right = rightIt.next();
        if (SimpleToken.START.equals(left) || SimpleToken.END.equals(left)) {
          // skip start and end Token in variant graph
          continue;
        }
        tokenMatches.add(tokenMatchTransform.apply(new Tuple<Token>(left, right)));
      }

      for (Tuple<Token> tokenMatch : tokenMatches) {
        final Token previousMatch = previousMatches.get(tokenMatch.left);
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

  private List<Token> matchPhrase(List<Token> phrase, int expectedDirection) {
    final List<Token> matchedPhrase = Lists.newArrayList();

    Token lastMatch = null;
    int lastMatchIndex = 0;
    for (Token token : phrase) {
      if (lastMatch == null) {
        lastMatch = Iterables.get(matches.getAll().get(token), 0);
        lastMatchIndex = baseMatches.indexOf(lastMatch);
        matchedPhrase.add(lastMatch);
        continue;
      }
      boolean tokenMatched = false;
      for (Token match : matches.getAll().get(token)) {
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
