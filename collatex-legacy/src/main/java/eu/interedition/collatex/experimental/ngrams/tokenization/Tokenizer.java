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

package eu.interedition.collatex.experimental.ngrams.tokenization;

import java.util.Arrays;
import java.util.Iterator;

import eu.interedition.collatex.experimental.ngrams.data.Token;

// Note: this tokenizer is very simplistic..
// it does not support streaming
// it does not support trailing whitespace!
public class Tokenizer {
  private final Iterator<String> arrayIterator;
  private int counter;
  private final String sigil;

  public Tokenizer(final String sigil, final String words) {
    this.sigil = sigil;
    arrayIterator = Arrays.asList(words.split(" ")).iterator(); // TODO more chars!
    counter = 0;
  }

  public boolean hasNext() {
    return arrayIterator.hasNext();
  }

  public Token nextToken() {
    final String content = arrayIterator.next();
    final Token token = new Token(sigil, content, ++counter);
    return token;
  }
}
