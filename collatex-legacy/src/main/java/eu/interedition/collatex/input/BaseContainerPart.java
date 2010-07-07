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

package eu.interedition.collatex.input;

import java.util.List;

import com.google.common.collect.Lists;

// Note: this class started life as a duplicate of
// Phase; it will get a life of its own
// for instance...
// anyway it should take BaseElements in
// TODO rename Word to Element!
public class BaseContainerPart<T extends BaseElement> {
  private final BaseContainer<T> _witness;
  private final int startPosition;
  private final int endPosition;
  private final int size;

  // TODO It is pretty obvious: too many parameters here!
  // Note: probably two constructors needed...
  // Note: one where the phrase resembles the words between two other words of the witness
  // Note: one where the start and end words of the phrase are given

  public BaseContainerPart(final BaseContainer<T> witness, final int _size, final int _startPosition, final int _endPosition) {
    this._witness = witness;
    this.size = _size;
    startPosition = _startPosition;
    endPosition = _endPosition;
  }

  public BaseContainerPart(final BaseContainer<T> witness, final T beginWord, final T endWord) {
    this._witness = witness;
    this.size = endWord.getEndPosition() - beginWord.getBeginPosition() + 1;
    this.startPosition = beginWord.getBeginPosition();
    this.endPosition = endWord.getEndPosition();
  }

  //TODO rename method to isEmpty!
  public boolean hasGap() {
    return size > 0;
  }

  @Override
  public String toString() {
    if (!hasGap()) {
      return "EMPTY!";
    }
    final List<String> words = Lists.newArrayList();
    for (int k = getBeginPosition(); k <= getEndPosition();) {
      final T word = _witness.getElementOnWordPosition(k);
      k += word.length();
      words.add(word.toString());
    }

    String replacementString = "";
    String divider = "";
    for (final String replacement : words) {
      replacementString += divider + replacement;
      divider = " ";
    }
    return replacementString;
  }

  public BaseContainer<T> getWitness() {
    return _witness;
  }

  public int getBeginPosition() {
    return startPosition;
  }

  public int getEndPosition() {
    return endPosition;
  }

  public List<T> getWords() {
    final List<T> words = Lists.newArrayList();
    for (int k = getBeginPosition(); k <= getEndPosition();) {
      final T word = getWitness().getElementOnWordPosition(k);
      k += word.length();
      words.add(word);
    }
    return words;
  }

  public T getFirstWord() {
    return getWords().get(0);
  }
}
