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

package eu.interedition.collatex.input;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import eu.interedition.collatex.TokenNormalizer;
import eu.interedition.collatex.Tokenizer;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.Token;

import java.util.Arrays;
import java.util.List;

/**
 * A very simplistic tokenizer.
 * 
 * <p>
 * It does not support streaming it does not support trailing whitespace!
 * </p>
 */
public class WhitespaceTokenizer implements Tokenizer {
  private TokenNormalizer tokenNormalizer = new DefaultTokenNormalizer();

  @Override
  public List<Token> tokenize(final Witness witness, String content) {
    return Lists.newArrayList(Iterables.transform(Arrays.asList(content.split("\\s+")), new Function<String, Token>() {
      private int tokenCount = 0;

      @Override
      public Token apply(String input) {
        return new SimpleToken(witness, tokenCount++, input, tokenNormalizer.apply(input.trim()));
      }
    }));
  }
}
