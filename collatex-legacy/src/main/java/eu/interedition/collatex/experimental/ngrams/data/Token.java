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

package eu.interedition.collatex.experimental.ngrams.data;

import eu.interedition.collatex2.interfaces.IToken;

public class Token implements IToken {
  private final String sigil;
  private final String content;
  private final int position;

  // private String trailingWhitespace; // TODO
  // private int characterPosition; // TODO

  public Token(final String sigil, final String content, final int position) {
    this.sigil = sigil;
    this.content = content;
    this.position = position;
  }

  public String getSigil() {
    return sigil;
  }

  public String getContent() {
    return content;
  }

  public int getPosition() {
    return position;
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof Token)) {
      return false;
    }
    final Token token = (Token) obj;
    final boolean result = sigil.equals(token.sigil) && content.equals(token.content) && position == token.position;
    return result;
  }

  @Override
  public int hashCode() {
    return content.hashCode();
  }

  @Override
  public boolean isNear(IToken b) {
    throw new RuntimeException("LEGACY CLASS DO NOT USE!");
  }

}
