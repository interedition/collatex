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

package eu.interedition.collatex2.implementation.input;

import eu.interedition.collatex2.interfaces.IToken;

public class Token implements IToken {
  private String content;

  // private String trailingWhitespace; // TODO
  // private int characterPosition; // TODO

  public Token() {
  }

  public Token(IToken other) {
    this(other.getContent());
  }

  public Token(final String content) {
    this.content = content;
  }

  @Override
  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  @Override
  public boolean equals(final Object obj) {
    if ((obj != null) && (obj instanceof Token)) {
      final Token token = (Token) obj;
      return content.equals(token.content);
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    int hc = 17;
    hc = hc * 59 + content.hashCode();
    return hc;
  }

}
