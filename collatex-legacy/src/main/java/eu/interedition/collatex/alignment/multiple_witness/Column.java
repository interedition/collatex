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

package eu.interedition.collatex.alignment.multiple_witness;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex.input.BaseElement;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Word;
import eu.interedition.collatex2.interfaces.ColumnState;

public class Column<T extends BaseElement> {

  // TODO rename Word to Element
  protected final Map<String, T> wordsProWitness;
  private final List<T> variants; // TODO rename to unique words!
  private ColumnState state;

  public Column(final T word) {
    wordsProWitness = Maps.newLinkedHashMap();
    variants = Lists.newLinkedList();
    initColumn(word);
  }

  private void initColumn(final T word) {
    wordsProWitness.put(word.getWitnessId(), word);
    variants.add(word);
    state = ColumnState.NEW;
  }

  public void addMatch(final T word) {
    wordsProWitness.put(word.getWitnessId(), word);
    state = state.addMatch();
  }

  public void addVariant(final T word) {
    wordsProWitness.put(word.getWitnessId(), word);
    variants.add(word);
    state = state.addVariant();
  }

  public void toXML(final StringBuilder builder) {
  // TODO Auto-generated method stub

  }

  @Override
  public String toString() {
    final Collection<T> values = wordsProWitness.values();
    StringBuilder result = new StringBuilder();
    StringBuilder delim = new StringBuilder();
    for (final T word : values) {
      result.append(delim).append(word.getOriginal());
      delim.append(" ");
    }
    return result.toString();
  }

  public boolean containsWitness(final Segment witness) {
    return wordsProWitness.containsKey(witness.id);
  }

  public T getWord(final Segment witness) {
    if (!containsWitness(witness)) {
      throw new NoSuchElementException();
    }
    final T result = wordsProWitness.get(witness.id);
    return result;
  }

  public Collection<T> getWords() {
    final Collection<T> values = wordsProWitness.values();
    return values;
  }

  public void addToSuperbase(final Superbase superbase) {
    for (final T variant : variants) {
      superbase.addWord((Word) variant, this);
    }
  }

  public ColumnState getColumnState() {
    return state;
  }

  public Set<String> getSigla() {
    return wordsProWitness.keySet();
  }

  public List<T> getUniqueElements() {
    return variants;
  }

  public boolean containsWitness(final String witnessId) {
    return wordsProWitness.containsKey(witnessId);
  }

  public T getWord(final String witnessId) {
    if (!containsWitness(witnessId)) {
      throw new NoSuchElementException();
    }
    final T result = wordsProWitness.get(witnessId);
    return result;
  }

}
