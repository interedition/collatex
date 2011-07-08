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

package eu.interedition.collatex2.implementation.input.tokenization;

import com.google.common.base.Preconditions;
import eu.interedition.collatex2.implementation.containers.witness.WitnessToken;
import eu.interedition.collatex2.interfaces.IToken;
import eu.interedition.collatex2.interfaces.ITokenNormalizer;
import eu.interedition.collatex2.interfaces.ITokenizer;

import java.util.Iterator;

/**
 * A very simplistic tokenizer.
 * <p/>
 * <p>
 * It does not support streaming it does not support trailing whitespace!
 * </p>
 */
public class AlternateWhitespaceTokenizer implements ITokenizer {

  private ITokenNormalizer normalizer;

  public void setNormalizer(ITokenNormalizer normalizer) {
    this.normalizer = normalizer;
  }

  @Override
  public Iterable<IToken> tokenize(String content) {
    final char[] charContent = content.toCharArray();
    return new Iterable<IToken>() {

      @Override
      public Iterator<IToken> iterator() {
        return new Iterator<IToken>() {
          private int offset = 0;
          private int tokenCount = 0;
          private String token = null;

          @Override
          public boolean hasNext() {
            while (offset < charContent.length && isTokenBoundary(charContent[offset])) {
              offset++;
            }

            final int start = offset;
            while (offset < charContent.length && !isTokenBoundary(charContent[offset])) {
              offset++;
            }
            while (offset < charContent.length && isTokenBoundary(charContent[offset])) {
              offset++;
            }
            final int tokenLength = offset - start;
            token = (tokenLength == 0 ? null : new String(charContent, start, tokenLength));

            if (token == null) {
              return false;
            } else {
              tokenCount++;
              return true;
            }
          }

          @Override
          public IToken next() {
            Preconditions.checkState(token != null);
            IToken witnessToken = new WitnessToken(token, tokenCount, token);
            if (normalizer != null) {
              witnessToken = normalizer.apply(witnessToken);
            }
            return witnessToken;
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }

  protected boolean isTokenBoundary(char c) {
    return Character.isWhitespace(c);
  }
}
