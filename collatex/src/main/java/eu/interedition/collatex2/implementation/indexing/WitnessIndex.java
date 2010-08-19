package eu.interedition.collatex2.implementation.indexing;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.interedition.collatex2.input.Phrase;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.ITokenIndex;

public class WitnessIndex implements ITokenIndex {
  List<IPhrase> phraseCollection = Lists.newArrayList();
  private static final Function<INormalizedToken, IPhrase> TOKEN_TO_PHRASE = new Function<INormalizedToken, IPhrase>() {
    @Override
    public IPhrase apply(final INormalizedToken token) {
      return new Phrase(Lists.newArrayList(token));
    }
  };
  private static final Logger LOG = LoggerFactory.getLogger(WitnessIndex.class);

  public WitnessIndex(final List<IPhrase> _phraseBag) {
    this.phraseCollection = _phraseBag;
  }

  public WitnessIndex(final IWitness witness, final List<String> repeatingTokens) {
    final List<INormalizedToken> tokens = witness.getTokens();
    phraseCollection.addAll(getUniqueTokensAsPhrases(tokens, repeatingTokens));
    phraseCollection.addAll(getUniquePhrasesFromRepeatingTokens(tokens, repeatingTokens));
  }

  @SuppressWarnings("boxing")
  private Collection<IPhrase> getUniquePhrasesFromRepeatingTokens(final List<INormalizedToken> tokens, final List<String> repeatingTokens) {
    final List<IPhrase> phraseCollection = Lists.newArrayList();
    final Set<Integer> positionsInPhrases = Sets.newHashSet();

    for (final INormalizedToken token : tokens) {
      LOG.debug(token.getContent());
      if (!positionsInPhrases.contains(token.getPosition()) && repeatingTokens.contains(token.getNormalized())) {
        phraseCollection.add(findUniquePhraseToTheLeft(token, repeatingTokens, tokens));
        phraseCollection.add(findUniquePhraseToTheRight(token, repeatingTokens, tokens));
        //        phraseCollection.add(leftExpandedPhrase(token, tokens, repeatingTokens, positionsInPhrases));
        //        phraseCollection.add(rightExpandedPhrase(token, tokens, repeatingTokens, positionsInPhrases));
      } else {
        positionsInPhrases.add(token.getPosition());
      }
    }
    LOG.debug(phraseCollection.toString());
    return phraseCollection;
  }

  private static IPhrase findUniquePhraseToTheLeft(final INormalizedToken token, final List<String> repeatingTokens, final List<INormalizedToken> tokens) {
    // combine to the left
    final IPhrase phrase = new Phrase(Lists.newArrayList(token));
    boolean found = false;
    for (int i = token.getPosition() - 1; !found && i > 0; i--) {
      final INormalizedToken leftToken = tokens.get(i - 1);
      final String normalizedNeighbour = leftToken.getNormalized();
      found = !repeatingTokens.contains(normalizedNeighbour);
      phrase.addTokenToLeft(leftToken);
    }
    if (!found) {
      phrase.addTokenToLeft(new NullToken(1, token.getSigil()));
    }
    return phrase;
  }

  private static IPhrase findUniquePhraseToTheRight(final INormalizedToken token, final List<String> repeatingTokens, final List<INormalizedToken> tokens) {
    final IPhrase phrase = new Phrase(Lists.newArrayList(token));
    boolean found = false;
    for (int i = token.getPosition() + 1; !found && i < tokens.size() + 1; i++) {
      final INormalizedToken rightToken = tokens.get(i - 1);
      final String normalizedNeighbour = rightToken.getNormalized();
      found = !repeatingTokens.contains(normalizedNeighbour);
      phrase.addTokenToRight(rightToken);
    }
    if (!found) {
      phrase.addTokenToRight(new NullToken(tokens.size(), token.getSigil()));
    }
    return phrase;
  }

  @SuppressWarnings("boxing")
  private IPhrase leftExpandedPhrase(final INormalizedToken token, final List<INormalizedToken> tokens, final Collection<String> repeatingTokens, final Set<Integer> tokenPositionsInPhrase) {
    final List<INormalizedToken> tokenlist = Lists.newArrayList(token);
    //    tokenPositionsInPhrase.add(token.getPosition());
    final String sigil = token.getSigil();
    INormalizedToken leftMostToken = token;
    do {
      final int leftPosition = leftMostToken.getPosition() - 1;
      LOG.debug(Integer.toString(leftPosition));
      final INormalizedToken leftToken;
      if (leftPosition > 0) {
        leftToken = tokens.get(leftPosition - 1);
        //        tokenPositionsInPhrase.add(leftPosition);
      } else {
        leftToken = new NullToken(leftMostToken.getPosition(), sigil);
      }
      tokenlist.add(0, leftToken);
      leftMostToken = leftToken;
  //    LOG.debug(leftMostToken.getNormalized());
    } while (repeatingTokens.contains(leftMostToken.getNormalized()));

    return new Phrase(tokenlist);
  }

  @SuppressWarnings("boxing")
  private IPhrase rightExpandedPhrase(final INormalizedToken token, final List<INormalizedToken> tokens, final Collection<String> repeatingTokens, final Set<Integer> tokenPositionsInPhrase) {
    final List<INormalizedToken> tokenlist = Lists.newArrayList(token);
    //    tokenPositionsInPhrase.add(token.getPosition());
    INormalizedToken rightMostToken = token;
    do {
      final int rightPosition = rightMostToken.getPosition() + 1;
   //   LOG.debug(Integer.toString(rightPosition));
      final INormalizedToken rightToken = (rightPosition < tokens.size()) ? tokens.get(rightPosition) : new NullToken(rightMostToken.getPosition(), token.getSigil());
      tokenlist.add(rightToken);
      //      tokenPositionsInPhrase.add(rightPosition - 1);
      rightMostToken = rightToken;
    } while (repeatingTokens.contains(rightMostToken.getNormalized()));

    return new Phrase(tokenlist);
  }

  private List<IPhrase> getUniqueTokensAsPhrases(final List<INormalizedToken> tokens, final Collection<String> repeatingTokens) {
    final Predicate<INormalizedToken> tokenIsUnique = new Predicate<INormalizedToken>() {
      @Override
      public boolean apply(final INormalizedToken t) {
        return !repeatingTokens.contains(t.getNormalized());
      }
    };
    final List<IPhrase> uniqueTokensAsPhrases = Lists.newArrayList(transform(filter(tokens, tokenIsUnique), TOKEN_TO_PHRASE));
    return uniqueTokensAsPhrases;
  }

  public static final Function<IPhrase, String> PHRASE_TO_NORMALIZED = new Function<IPhrase, String>() {
    @Override
    public String apply(final IPhrase phrase) {
      return phrase.getNormalized();
    }
  };

  @Override
  public boolean contains(final String normalizedPhrase) {
    final List<String> phrasesInIndex = Lists.newArrayList(transform(phraseCollection, PHRASE_TO_NORMALIZED));
    LOG.debug(phrasesInIndex.toString());
    return phrasesInIndex.contains(normalizedPhrase);
  }

  @Override
  public int size() {
    return phraseCollection.size();
  }

  @Override
  public IPhrase getPhrase(String normalized) {
    for (IPhrase phrase : phraseCollection) {
      if (phrase.getNormalized().equals(normalized)) {
        return phrase;
      }
    }
    throw new RuntimeException("Phrase NOT found!");
  }

  @Override
  public Set<String> keys() {
    final Set<String> normalizedPhrases = Sets.newLinkedHashSet(transform(phraseCollection, PHRASE_TO_NORMALIZED));
    return normalizedPhrases;
  }

}
