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

package eu.interedition.collatex2.legacy.indexing;

import eu.interedition.collatex2.implementation.input.NormalizedToken;
import eu.interedition.collatex2.implementation.input.Token;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.ITokenNormalizer;


public class BiGram {
  // NOTE: BiGram could become an extend version of NormalizedToken!
  private final INormalizedToken _previous;
  private final INormalizedToken _next;

  public BiGram(final INormalizedToken previous, final INormalizedToken next) {
    this._previous = previous;
    this._next = next;
  }

  public String getNormalized() {
    return _previous.getNormalized() + " " + _next.getNormalized();
  }

  public INormalizedToken getFirstToken() {
    return _previous;

  }

  public INormalizedToken getLastToken() {
    return _next;
  }

  public static BiGram create(final Token token, final Token token2, ITokenNormalizer normalizer) {
    INormalizedToken tokenA = new NormalizedToken(token.getSigil(), token.getContent(), -1, normalizer.apply(token));
    INormalizedToken tokenB = new NormalizedToken(token2.getSigil(), token2.getContent(), -1, normalizer.apply(token2));
    return new BiGram(tokenA, tokenB);
  }

  public boolean contains(final Token token) {
    return getFirstToken().equals(token) || getLastToken().equals(token);
  }
}
