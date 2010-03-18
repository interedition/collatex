package eu.interedition.collatex2.implementation.indexing;

import static com.google.common.collect.Iterables.transform;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

import eu.interedition.collatex2.implementation.input.Phrase;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.IWitnessIndex;

public class WitnessIndex implements IWitnessIndex {
  Multiset<IPhrase> phraseBag = Multisets.newHashMultiset();

  public WitnessIndex(final IWitness witness) {
    Multimap<String, IPhrase> phraseMap = Multimaps.newArrayListMultimap();
    final List<INormalizedToken> tokens = witness.getTokens();
    for (final INormalizedToken token : tokens) {
      phraseMap.put(token.getNormalized(), new Phrase(Lists.newArrayList(token)));
    }
    final Multimap<String, IPhrase> newPhraseMap = Multimaps.newArrayListMultimap();
    for (final String phraseId : phraseMap.keySet()) {
      final Collection<IPhrase> phrases = phraseMap.get(phraseId);
      if (phrases.size() > 1) {
        for (final IPhrase phrase : phrases) {
          final int beforePosition = phrase.getBeginPosition() - 1;
          final int afterPosition = phrase.getEndPosition();
          final INormalizedToken beforeToken = (beforePosition > 0) ? tokens.get(beforePosition - 1) : new NullToken(phrase.getBeginPosition(), phrase.getSigil());
          final INormalizedToken afterToken = (afterPosition < tokens.size()) ? tokens.get(afterPosition) : new NullToken(phrase.getEndPosition(), phrase.getSigil());
          final INormalizedToken phraseToken = phrase.getFirstToken();
          final IPhrase leftExpandedPhrase = new Phrase(Lists.newArrayList(beforeToken, phraseToken));
          final IPhrase rightExpandedPhrase = new Phrase(Lists.newArrayList(phraseToken, afterToken));
          newPhraseMap.put(leftExpandedPhrase.getNormalized(), leftExpandedPhrase);
          newPhraseMap.put(rightExpandedPhrase.getNormalized(), rightExpandedPhrase);
        }
      } else {
        newPhraseMap.put(phraseId, phrases.iterator().next());
      }
    }
    phraseMap = newPhraseMap;
    //TODO: iterate until all tokens are unique

    phraseBag.addAll(phraseMap.values());
  }

  private static final Function<IPhrase, String> PHRASE_TO_NORMALIZED = new Function<IPhrase, String>() {
    @Override
    public String apply(final IPhrase phrase) {
      return phrase.getNormalized();
    }
  };

  @Override
  public boolean contains(final String normalizedPhrase) {
    final List<String> phrasesInIndex = Lists.newArrayList(transform(phraseBag.elementSet(), PHRASE_TO_NORMALIZED));
    return phrasesInIndex.contains(normalizedPhrase);
  }

  @Override
  public int size() {
    return phraseBag.size();
  }
}
