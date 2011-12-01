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

import eu.interedition.collatex.interfaces.IToken;
import eu.interedition.collatex.interfaces.IWitness;

public class Token implements IToken {
  private IWitness witness;
  private int index;
  private String content;
  private String trailingWhitespace;

  public Token(IWitness witness, int index, String content, String trailingWhitespace) {
    this.witness = witness;
    this.index = index;
    this.content = content;
    this.trailingWhitespace = trailingWhitespace;
  }

  public Token(Token other) {
    this(other.getWitness(), other.getIndex(), other.getContent(), other.getTrailingWhitespace());
  }

  public Token(IWitness witness, int index, String content) {
    this(witness, index, content, "");
  }

  public int getIndex() {
    return index;
  }

  @Override
  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
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
  public String toString() {
    return content;
  }

  public static String toString(Iterable<? extends IToken> tokens) {
    final StringBuilder content = new StringBuilder();
    for (final IToken token : tokens) {
      content.append(token.getContent()).append(" ");
    }
    return content.toString().trim();
  }
}
