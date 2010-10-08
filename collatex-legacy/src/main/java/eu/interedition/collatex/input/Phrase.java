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
import com.sd_editions.collatex.match.Subsegment;
@Deprecated
public class Phrase extends BaseElement {
  private final Segment witness;
  private final int startPosition;
  private final int endPosition;
  private final int size;
  private final Subsegment _subSegment;

  // TODO It is pretty obvious: too many parameters here!
  // Note: probably two constructors needed...
  // Note: one where the phrase resembles the words between two other words of the witness
  // Note: one where the start and end words of the phrase are given

  // THIS constructor is pretty close, and actually used!
  public Phrase(final Segment _witness, final Word beginWord, final Word endWord, final Subsegment subsegment) {
    this.witness = _witness;
    this.startPosition = beginWord.position;
    this.endPosition = endWord.position;
    this._subSegment = subsegment;
    this.size = endWord.position - beginWord.position + 1;
  }

  public Phrase(final int _startPosition, final int _endPosition, final Subsegment subsegment) {
    this.startPosition = _startPosition;
    this.endPosition = _endPosition;
    this._subSegment = subsegment;
    this.size = endPosition - startPosition + 1;
    this.witness = null; // this is not wanted here!
  }

  @Override
  public String toString() {
    final List<String> words = Lists.newArrayList();
    for (int k = getStartPosition(); k <= getEndPosition(); k++) {
      final String word = witness.getElementOnWordPosition(k).toString();
      words.add(word);
    }

    StringBuilder replacementString = new StringBuilder();
    String divider = "";
    for (final String replacement : words) {
      replacementString.append(divider).append(replacement);
      divider = " ";
    }
    return replacementString.toString();
  }

  public Segment getWitness() {
    return witness;
  }

  public int getStartPosition() {
    return startPosition;
  }

  @Override
  public int getEndPosition() {
    return endPosition;
  }

  public List<Word> getWords() {
    final List<Word> words = Lists.newArrayList();
    for (int k = getStartPosition(); k <= getEndPosition(); k++) {
      final Word word = getWitness().getElementOnWordPosition(k);
      words.add(word);
    }
    return words;
  }

  public Word getFirstWord() {
    return getWords().get(0);
  }

  @Override
  public String getOriginal() {
    return toString();
  }

  @Override
  public int getBeginPosition() {
    return startPosition;
  }

  public Subsegment getSubsegment() {
    return _subSegment;
  }

  @Override
  public String getWitnessId() {
    return getFirstWord().getWitnessId();
  }

  @Override
  public int length() {
    return size;
  }

  public Word getLastWord() {
    return getWords().get(length() - 1);
  }

  public String getNormalized() {
    // TODO Auto-generated method stub
    return null;
  }
}
