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

public class Token implements IToken {
  private String content;
  private String trailingWhitespace;

  public Token() {
    this(null, "");
  }

  public Token(String content, String trailingWhitespace) {
    this.content = content;
    this.trailingWhitespace = trailingWhitespace;
  }

  public Token(IToken other) {
    this(other.getContent(), other.getTrailingWhitespace());
  }

  public Token(final String content) {
    this(content, "");
  }

  @Override
  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
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
