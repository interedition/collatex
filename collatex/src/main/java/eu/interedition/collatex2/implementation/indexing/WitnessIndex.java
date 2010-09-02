/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex2.implementation.indexing;

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
import eu.interedition.collatex2.interfaces.ITokenIndex;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.IWitnessIndex;

public class WitnessIndex implements IWitnessIndex, ITokenIndex {
  private final Map<String, IPhrase> map;

  //NOTE: repeated Tokens are ignored and can be deleted later!
  public WitnessIndex(IWitness witness, List<String> repeatedTokens) {
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
