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
    // do the unigram indexing... remove duplicates!
    final Multimap<String, INormalizedToken> normalizedTokenMap = ArrayListMultimap.create();
    for (final INormalizedToken token : witness.getTokens()) {
      normalizedTokenMap.put(token.getNormalized(), token);
    }
    for (final String key : normalizedTokenMap.keySet()) {
      final Collection<INormalizedToken> tokenCollection = normalizedTokenMap.get(key);
      if (tokenCollection.size() == 1) {
        List<INormalizedToken> firstToken = Lists.newArrayList(normalizedTokenMap.get(key));
        map.put(key, new Phrase(firstToken));
      }
    }

    // do the bidgram indexing TODO: remove duplicates in index!
    BiGramIndex bigramIndex = BiGramIndex.create(witness);
    List<BiGram> biGrams = bigramIndex.getBiGrams();
    for (BiGram gram : biGrams) {
      map.put(gram.getNormalized(), new Phrase(Lists.newArrayList(gram.getFirstToken(), gram.getLastToken())));
    }
  }

  @Override
  public boolean contains(String normalized) {
    return map.containsKey(normalized);
  }

  @Override
  public Collection<IPhrase> getPhrases() {
    throw new RuntimeException("Remove this method!");
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
