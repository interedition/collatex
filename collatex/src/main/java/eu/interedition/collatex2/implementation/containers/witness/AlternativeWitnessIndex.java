package eu.interedition.collatex2.implementation.containers.witness;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import eu.interedition.collatex2.input.Phrase;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.IWitnessIndex;
import eu.interedition.collatex2.legacy.indexing.BiGram;
import eu.interedition.collatex2.legacy.indexing.BiGramIndex;
import eu.interedition.collatex2.legacy.indexing.NGram;

//TODO: To make this WitnessIndex work 
//TODO: make an AlternativeVariantGraphIndex that works
//TODO: in a similar way
//TODO: use the AlternativeTokenIndexMatcher
//TODO: change the filtering after the matching
//TODO: to work on vertices instead of columns
public class AlternativeWitnessIndex implements IWitnessIndex {
  private final Map<String, IPhrase> map;

  //NOTE: repeated Tokens are ignored and can be deleted later!
  public AlternativeWitnessIndex(IWitness witness, List<String> repeatedTokens) {
    this.map = Maps.newLinkedHashMap();
    // do the unigram indexing... 
    final Multimap<String, IPhrase> normalizedTokenMap = ArrayListMultimap.create();
    for (final INormalizedToken token : witness.getTokens()) {
      normalizedTokenMap.put(token.getNormalized(), new Phrase(Lists.newArrayList(token)));
    }
    // do the bigram indexing 
    BiGramIndex bigramIndex = BiGramIndex.create(witness);
    List<BiGram> biGrams = bigramIndex.getBiGrams();
    for (BiGram gram : biGrams) {
      normalizedTokenMap.put(gram.getNormalized(), new Phrase(Lists.newArrayList(gram.getFirstToken(), gram.getLastToken())));
    }
    // do the trigram indexing
    if (!biGrams.isEmpty()) {
      List<BiGram> bigramsTodo = biGrams.subList(1, biGrams.size());
      BiGram current = biGrams.get(0);
      for (BiGram nextBigram : bigramsTodo) {
        NGram ngram = NGram.create(current);
        ngram.add(nextBigram);
        current = nextBigram;
        normalizedTokenMap.put(ngram.getNormalized(), new Phrase(Lists.newArrayList(ngram)));
      }
    }
    // remove duplicates in ngram index!
    for (final String key : normalizedTokenMap.keySet()) {
      final Collection<IPhrase> tokenCollection = normalizedTokenMap.get(key);
      if (tokenCollection.size() == 1) {
        List<IPhrase> firstPhrase = Lists.newArrayList(normalizedTokenMap.get(key));
        map.put(key, firstPhrase.get(0));
      }
    }
  }

  @Override
  public boolean contains(String normalized) {
    return map.containsKey(normalized);
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public IPhrase getPhrase(String key) {
    return map.get(key);
  }

  @Override
  public Set<String> keys() {
    return map.keySet();
  }
}
