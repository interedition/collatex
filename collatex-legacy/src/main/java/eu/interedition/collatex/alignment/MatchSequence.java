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

package eu.interedition.collatex.alignment;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.input.BaseElement;

public class MatchSequence<T extends BaseElement> {
  private final List<Match<T>> sequence;
  public final Integer code;

  public MatchSequence(final Integer _code, final Match<T>... matches) {
    sequence = Lists.newArrayList(matches);
    code = _code;
  }

  @Override
  public String toString() {
    return sequence.toString();
  }

  public void add(final Match<T> match) {
    sequence.add(match);
  }

  public boolean isEmpty() {
    return sequence.isEmpty();
  }

  public Match<T> getFirstMatch() {
    return sequence.get(0);
  }

  // TODO rename to getWitnessBeginPosition!
  @SuppressWarnings("boxing")
  public Integer getSegmentPosition() {
    return getFirstWitnessWord().getBeginPosition();
  }

  // TODO rename to getBaseWitnessBeginPosition!
  @SuppressWarnings("boxing")
  public Integer getBasePosition() {
    return getFirstBaseWord().getBeginPosition();
  }

  //TODO rename Word to Element!
  private T getFirstWitnessWord() {
    return getFirstMatch().getWitnessWord();
  }

  //TODO rename Word to Element!
  private T getFirstBaseWord() {
    return getFirstMatch().getBaseWord();
  }

  public String baseToString() {
    String result = "";
    String delimiter = "";
    for (int i = 0; i < sequence.size(); i++) {
      final T baseWord = sequence.get(i).getBaseWord();
      if (i > 0 && (baseWord.getBeginPosition() - sequence.get(i - 1).getBaseWord().getBeginPosition()) > 1) {
        result += delimiter + "...";
      }
      result += delimiter + baseWord.toString();
      delimiter = " ";
    }
    return result;
  }

  public List<Match<T>> getMatches() {
    return sequence;
  }

  public Match<T> getLastMatch() {
    return sequence.get(sequence.size() - 1);
  }
}
