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

package eu.interedition.collatex.implementation.input;

import eu.interedition.collatex.interfaces.INormalizedToken;
import eu.interedition.collatex.interfaces.IWitness;

public class NormalizedToken extends Token implements INormalizedToken {
  public static final NormalizedToken START = new NormalizedToken(Witness.SUPERBASE, -1, "", "#");
  public static final NormalizedToken END = new NormalizedToken(Witness.SUPERBASE, Integer.MAX_VALUE, "", "#");

  private String normalized;

  public NormalizedToken(NormalizedToken other) {
    super(other);
    this.normalized = other.getNormalized();
  }

  public NormalizedToken(IWitness witness, int index, String content, String normalized) {
    super(witness, index, content);
    this.normalized = normalized;
  }

  public NormalizedToken(Token token, String normalized) {
    super(token);
    this.normalized = normalized;
  }

  @Override
  public String getNormalized() {
    return normalized;
  }


  public void setNormalized(String normalized) {
    this.normalized = normalized;
  }

  @Override
  public String toString() {
    return new StringBuilder(getWitness().toString()).append(":'").append(getNormalized()).append("'").toString();
  }

  public static String toString(Iterable<? extends INormalizedToken> tokens) {
    final StringBuilder normalized = new StringBuilder();
    for (final INormalizedToken token : tokens) {
      normalized.append(token.getNormalized()).append(" ");
    }
    return normalized.toString().trim();
  }

  @Override
  public int compareTo(INormalizedToken o) {
    final int witnessComparison = getWitness().compareTo(o.getWitness());
    if (witnessComparison != 0) {
      return witnessComparison;
    }
    return (getIndex() - ((Token) o).getIndex());
  }
}
