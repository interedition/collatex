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

import com.google.common.base.Objects;
import eu.interedition.collatex.interfaces.Token;
import eu.interedition.collatex.interfaces.IWitness;

public class SimpleToken implements Token {
  public static final SimpleToken START = new SimpleToken(Witness.SUPERBASE, -1, "", "#");
  public static final SimpleToken END = new SimpleToken(Witness.SUPERBASE, Integer.MAX_VALUE, "", "#");

  private IWitness witness;
  private int index;
  private String content;
  private String trailingWhitespace;
  private String normalized;

  public SimpleToken(IWitness witness, int index, String content, String normalized) {
    this.witness = witness;
    this.index = index;
    this.content = content;
    this.normalized = normalized;
    this.trailingWhitespace = "";
  }

  public int getIndex() {
    return index;
  }

  @Override
  public String getContent() {
    return content;
  }

  @Override
  public IWitness getWitness() {
    return witness;
  }

  @Override
  public String getTrailingWhitespace() {
    return trailingWhitespace;
  }

  public void setTrailingWhitespace(String whitespace) {
    this.trailingWhitespace = whitespace;
  }


  @Override
  public String getNormalized() {
    return normalized;
  }


  @Override
  public String toString() {
    return new StringBuilder(witness.toString()).append(":").append(index).append(":'").append(normalized).append("'").toString();
  }

  public static String toString(Iterable<? extends Token> tokens) {
    final StringBuilder normalized = new StringBuilder();
    for (final Token token : tokens) {
      normalized.append(token.getNormalized()).append(" ");
    }
    return normalized.toString().trim();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getWitness(), getIndex());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof SimpleToken) {
      SimpleToken other = (SimpleToken) obj;
      return getIndex() == other.getIndex() && getWitness().equals(other.getWitness());
    }
    return super.equals(obj);
  }

  @Override
  public int compareTo(Token o) {
    final int witnessComparison = witness.compareTo(o.getWitness());
    if (witnessComparison != 0) {
      return witnessComparison;
    }
    return (index - ((SimpleToken) o).getIndex());
  }
}
