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

import java.util.Arrays;
import java.util.Iterator;

import eu.interedition.collatex.interfaces.IToken;
import eu.interedition.collatex.interfaces.ITokenizer;
import eu.interedition.collatex.interfaces.IWitness;

/**
 * A very simplistic tokenizer.
 * 
 * <p>
 * It does not support streaming it does not support trailing whitespace!
 * </p>
 */
public class WhitespaceTokenizer implements ITokenizer {

  @Override
  public Iterable<IToken> tokenize(final IWitness witness, String content) {
    final Iterator<String> tokenIterator = Arrays.asList(content.split("\\s+")).iterator();
    return new Iterable<IToken>() {

      @Override
      public Iterator<IToken> iterator() {
        return new Iterator<IToken>() {

          private int tokenCount = 0;

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }

          @Override
          public IToken next() {
            return new Token(witness, tokenCount++, tokenIterator.next());
          }

          @Override
          public boolean hasNext() {
            return tokenIterator.hasNext();
          }
        };
      }
    };
  }
}
