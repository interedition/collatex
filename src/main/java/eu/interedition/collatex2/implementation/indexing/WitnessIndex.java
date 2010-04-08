package eu.interedition.collatex2.implementation.indexing;

import static com.google.common.collect.Iterables.transform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.mortbay.log.Log;

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
  Multiset<IPhrase> phraseBag = Multisets.newTreeMultiset();

  public WitnessIndex(final Multiset<IPhrase> _phraseBag) {
    this.phraseBag = _phraseBag;
  }

  public WitnessIndex(final IWitness witness) {
    final List<INormalizedToken> tokens = witness.getTokens();
    final Multimap<String, IPhrase> seedlings = seed(tokens);
    final Multimap<String, IPhrase> crop = grow(seedlings, tokens);
    phraseBag.addAll(harvest(crop));
  }

  public static final Function<IPhrase, String> PHRASE_TO_NORMALIZED = new Function<IPhrase, String>() {
    @Override
    public String apply(final IPhrase phrase) {
      return phrase.getNormalized();
    }
  };

  @Override
  public boolean contains(final String normalizedPhrase) {
    final List<String> phrasesInIndex = Lists.newArrayList(transform(phraseBag.elementSet(), PHRASE_TO_NORMALIZED));
    Log.info(phrasesInIndex.toString());
    return phrasesInIndex.contains(normalizedPhrase);
  }

  @Override
  public int size() {
    return phraseBag.size();
  }

  @Override
  public Collection<IPhrase> getPhrases() {
    return phraseBag;
  }

  private Multimap<String, IPhrase> seed(final List<INormalizedToken> tokens) {
    final Multimap<String, IPhrase> phraseMap = Multimaps.newHashMultimap();
    for (final INormalizedToken token : tokens) {
      phraseMap.put(token.getNormalized(), new Phrase(Lists.newArrayList(token)));
    }
    return phraseMap;
  }

  private Multimap<String, IPhrase> grow(final Multimap<String, IPhrase> _phraseMap, final List<INormalizedToken> tokens) {
    Multimap<String, IPhrase> phraseMap = _phraseMap;
    do {
      final Multimap<String, IPhrase> newPhraseMap = Multimaps.newHashMultimap();
      //      Log.info("keys = " + phraseMap.keySet());
      for (final String phraseId : phraseMap.keySet()) {
        final Collection<IPhrase> phrases = phraseMap.get(phraseId);
        //        Log.info("phrases = " + phrases.toString());
        if (phrases.size() > 1) {
          addExpandedPhrases(newPhraseMap, phrases, tokens/*, phraseMap*/);
        } else {
          final IPhrase phrase = phrases.iterator().next();
          //          if (phrase.size() == 1) {
          newPhraseMap.put(phraseId, phrase);
          //          }
        }
        //        Log.info("newPhraseMap = " + newPhraseMap.toString());
        //        Log.info("");
      }
      phraseMap = newPhraseMap;
      //      Log.info("phraseMap.entries().size() = " + String.valueOf(phraseMap.entries().size()));
      //      Log.info("phraseMap.keySet().size() = " + String.valueOf(phraseMap.keySet().size()));
      //      Log.info("");
    } while (phraseMap.entries().size() > phraseMap.keySet().size());
    return phraseMap;
  }

  private void addExpandedPhrases(final Multimap<String, IPhrase> newPhraseMap, final Collection<IPhrase> phrases, final List<INormalizedToken> tokens) {
    for (final IPhrase phrase : phrases) {
      final int beforePosition = phrase.getBeginPosition() - 1;
      final int afterPosition = phrase.getEndPosition();

      final INormalizedToken beforeToken = (beforePosition > 0) ? tokens.get(beforePosition - 1) : new NullToken(phrase.getBeginPosition(), phrase.getSigil());
      final INormalizedToken afterToken = (afterPosition < tokens.size()) ? tokens.get(afterPosition) : new NullToken(phrase.getEndPosition(), phrase.getSigil());

      final ArrayList<INormalizedToken> leftExpandedTokenList = Lists.newArrayList(beforeToken);
      leftExpandedTokenList.addAll(phrase.getTokens());
      final IPhrase leftExpandedPhrase = new Phrase(leftExpandedTokenList);

      final ArrayList<INormalizedToken> rightExpandedTokenList = Lists.newArrayList(phrase.getTokens());
      rightExpandedTokenList.add(afterToken);
      final IPhrase rightExpandedPhrase = new Phrase(rightExpandedTokenList);

      final String leftPhraseId = leftExpandedPhrase.getNormalized();
      newPhraseMap.put(leftPhraseId, leftExpandedPhrase);

      final String rightPhraseId = rightExpandedPhrase.getNormalized();
      newPhraseMap.put(rightPhraseId, rightExpandedPhrase);
    }
  }

  private List<IPhrase> harvest(final Multimap<String, IPhrase> phraseMap) {
    final List<IPhrase> values = Lists.newArrayList(phraseMap.values());
    Collections.sort(values, Phrase.PHRASECOMPARATOR);
    return values;
  }

}
