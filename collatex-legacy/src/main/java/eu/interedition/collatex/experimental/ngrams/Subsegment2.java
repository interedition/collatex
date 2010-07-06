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

package eu.interedition.collatex.experimental.ngrams;

import java.util.Map;

import com.google.common.collect.Maps;

import eu.interedition.collatex2.interfaces.INormalizedToken;

public class Subsegment2 {

  private final String _normalized;
  //Note: it does not have to be a map; could
  //also be a list; but would be slower!
  private final Map<String, BiGram> _sigilToPhrase;

  public Subsegment2(final String normalized, final BiGram tuple) {
    this._normalized = normalized;
    this._sigilToPhrase = Maps.newLinkedHashMap();
    final String witnessId = tuple.getFirstToken().getSigil();
    _sigilToPhrase.put(witnessId, tuple);
  }

  public Subsegment2(final String normalized, final BiGram wordsA, final BiGram wordsB) {
    this._normalized = normalized;
    this._sigilToPhrase = Maps.newLinkedHashMap();
    _sigilToPhrase.put(wordsA.getFirstToken().getSigil(), wordsA);
    _sigilToPhrase.put(wordsB.getFirstToken().getSigil(), wordsB);
  }

  public String getNormalized() {
    return _normalized;
  }

  public boolean contains(final String sigil) {
    return _sigilToPhrase.containsKey(sigil);
  }

  public BiGram getPhraseFor(final String sigil) {
    if (!contains(sigil)) {
      throw new RuntimeException("This subsegment does not contain a phrase for " + sigil);
    }
    return _sigilToPhrase.get(sigil);
  }

  public INormalizedToken getFirstWordFor(final String sigil) {
    final BiGram phrase = getPhraseFor(sigil);
    return phrase.getFirstToken();
  }

  @Override
  public String toString() {
    final StringBuffer buffer = new StringBuffer();
    buffer.append(_normalized);
    for (final String sigil : _sigilToPhrase.keySet()) {
      final BiGram wordsTuple = _sigilToPhrase.get(sigil);
      buffer.append(" ");
      buffer.append(wordsTuple.getFirstToken().getSigil());
      buffer.append(": ");
      buffer.append(wordsTuple.getFirstToken().getPosition());
    }
    return buffer.toString();
  }
}
