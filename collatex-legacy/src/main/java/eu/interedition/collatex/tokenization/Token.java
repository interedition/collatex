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

package eu.interedition.collatex.tokenization;
@Deprecated
public class Token {
  private final String _original;
  private final String _text;
  private final String _punctuation;

  public Token(String orginal) {
    this(orginal, "", "");
  }

  public Token(String original, String text, String punctuation) {
    this._original = original;
    this._text = text;
    this._punctuation = punctuation;
  }

  public String getText() {
    return _text;
  }

  public String getPunctuation() {
    return _punctuation;
  }

  public String getOriginal() {
    return _original;
  }
}
