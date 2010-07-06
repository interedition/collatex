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

import java.util.Collection;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class WitnessSegmentPhrases extends BaseContainer<Phrase> {
  private final String _witnessId;
  private final List<Phrase> _phrases;

  public WitnessSegmentPhrases(final String witnessId, final Phrase... phrases) {
    _witnessId = witnessId;
    _phrases = Lists.newArrayList(phrases);
  }

  public WitnessSegmentPhrases(final String witnessId, final Collection<Phrase> phrases) {
    _witnessId = witnessId;
    _phrases = Lists.newArrayList(phrases);
  }

  public Phrase getPhraseOnPosition(final int i) {
    return _phrases.get(i - 1);
  }

  public String getWitnessId() {
    return _witnessId;
  }

  @Override
  public String toString() {
    return "WitnessSegmentPhrases(" + getWitnessId() + ", '" + Joiner.on("','").join(_phrases) + "')";
  }

  @Override
  public int wordSize() {
    return getPhrases().get(getPhrases().size() - 1).getEndPosition();
  }

  public int size() {
    return _phrases.size();
  }

  @Override
  public Phrase getElementOnWordPosition(final int k) {
    for (final Phrase phrase : getPhrases()) {
      if (phrase.getBeginPosition() == k) {
        return phrase;
      }
    }
    throw new RuntimeException("No element found on position: " + k);
  }

  public List<Phrase> getPhrases() {
    return _phrases;
  }

  public String toSpecialString() {
    final StringBuffer buffer = new StringBuffer();
    buffer.append("|");
    for (final Phrase phrase : _phrases) {
      // TODO could become get normalized1
      buffer.append(phrase.getOriginal());
      buffer.append("|");
    }
    return buffer.toString();
  }
}
