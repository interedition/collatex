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

package eu.interedition.collatex.general;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.experimental.ngrams.data.NormalizedToken;
import eu.interedition.collatex.experimental.ngrams.data.NormalizedWitness;
import eu.interedition.collatex.experimental.ngrams.data.Token;
import eu.interedition.collatex.experimental.ngrams.tokenization.Tokenizer;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class NormalizedWitnessBuilder {

  public static IWitness create(final String sigil, final String words) {
    final List<Token> tokens = tokenize(sigil, words);
    final List<INormalizedToken> normalizeds = normalize(tokens);
    final IWitness result = new NormalizedWitness(sigil, normalizeds);
    return result;
  }

  private static List<Token> tokenize(final String sigil, final String words) {
    final Tokenizer tokenizer = new Tokenizer(sigil, words);
    final List<Token> tokens = Lists.newArrayList();
    while (tokenizer.hasNext()) {
      final Token next = tokenizer.nextToken();
      tokens.add(next);
    }
    return tokens;
  }

  private static List<INormalizedToken> normalize(final List<Token> tokens) {
    final List<INormalizedToken> normalizeds = Lists.newArrayList();
    for (final Token token : tokens) {
      final INormalizedToken normalizedT = NormalizedToken.normalize(token);
      normalizeds.add(normalizedT);
    }
    return normalizeds;
  }

}
