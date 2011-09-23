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

package eu.interedition.collatex2.legacy.indexing;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex2.implementation.input.NullToken;
import eu.interedition.collatex2.implementation.input.Token;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class BiGramIndex implements Iterable<BiGram> {

  private final List<BiGram> biGrams1;

  public BiGramIndex(final List<BiGram> biGrams1) {
    this.biGrams1 = biGrams1;
  }

  public BiGramIndex() {
    this.biGrams1 = Lists.newArrayList();
  }

  // TODO remove!
  public Map<String, BiGram> normalize() {
    final Map<String, BiGram> biGramMapped1 = normalize(biGrams1);
    return biGramMapped1;
  }

  public static BiGramIndex create(final IWitness a) {
    return create(a.getTokens());
  }

//  // TODO replace calls to this method with calls to create!
//  // TODO make this method private
//  public static List<BiGram> calculate(final IWitness w) {
//    final List<INormalizedToken> tokens = w.getTokens();
//  }

  // TODO integrate the two static functions into one!
  private static Map<String, BiGram> normalize(final List<BiGram> ngrams) {
    final Map<String, BiGram> normalized = Maps.newLinkedHashMap();
    for (final BiGram ngram : ngrams) {
      normalized.put(ngram.getNormalized(), ngram);
    }
    return normalized;
  }

  public Set<String> keys() {
    final Map<String, BiGram> normalize = normalize(biGrams1);
    return normalize.keySet();
  }

  public BiGram get(final String key) {
    final Map<String, BiGram> normalize = normalize(biGrams1);
    return normalize.get(key);
  }

  // NOTE: this method is only used in tests! Make it less visible?
  public BiGramIndex removeBiGramsWithToken(final Token token) {
    final List<BiGram> result = Lists.newArrayList(Iterables.filter(biGrams1, new Predicate<BiGram>() {
      @Override
      public boolean apply(final BiGram bigram) {
        return !bigram.contains(token);
      }
    }));
    return new BiGramIndex(result);
  }

  public int size() {
    return biGrams1.size();
  }

  //NOTE: I am not pleased with this method... I wish other classes could iterate over this class!
  //TODO remove!
  public List<BiGram> getBiGrams() {
    return biGrams1;
  }

  public BiGram get(final int i) {
    return biGrams1.get(i);
  }

  // TODO make iterator read only!
  @Override
  public Iterator<BiGram> iterator() {
    return biGrams1.iterator();
  }

  public static BiGramIndex create(List<INormalizedToken> tokens) {
    if (tokens.isEmpty()) {
      return new BiGramIndex();
    }
    final List<INormalizedToken> tokensTodo = Lists.newArrayList(tokens);
    tokensTodo.add(new NullToken());
    INormalizedToken previous = new NullToken();
    final List<BiGram> bigrams = Lists.newArrayList();
    for (final INormalizedToken next : tokensTodo) {
      final BiGram tuple = new BiGram(previous, next);
      bigrams.add(tuple);
      previous = next;
    }
    return new BiGramIndex(bigrams);
  }
}