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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import eu.interedition.collatex2.input.Phrase;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.ITokenIndex;
import eu.interedition.collatex2.interfaces.IWitness;

public class NormalizedWitness implements Iterable<INormalizedToken>, IWitness {
  private final String sigil;
  private final List<INormalizedToken> tokens;

  public NormalizedWitness(final String sigil, final List<INormalizedToken> tokens) {
    this.sigil = sigil;
    this.tokens = tokens;
  }

  // Note: not pleased with this method! implement Iterable!
  /* (non-Javadoc)
   * @see eu.interedition.collatex.experimental.ngrams.data.IRealWitness#getTokens()
   */
  public List<INormalizedToken> getTokens() {
    return tokens;
  }

  /* (non-Javadoc)
   * @see eu.interedition.collatex.experimental.ngrams.data.IRealWitness#getSigil()
   */
  public String getSigil() {
    return sigil;
  }

  // TODO check whether iterator.remove() throws exception!
  /* (non-Javadoc)
   * @see eu.interedition.collatex.experimental.ngrams.data.IRealWitness#iterator()
   */
  @Override
  public Iterator<INormalizedToken> iterator() {
    return tokens.iterator();
  }

  /* (non-Javadoc)
  * @see eu.interedition.collatex.experimental.ngrams.data.IRealWitness#size()
  */
  public int size() {
    return tokens.size();
  }

  @Override
  public IPhrase createPhrase(final int startPosition, final int endPosition) {
    return new Phrase(tokens.subList(startPosition - 1, endPosition));
  }

  @Override
  public List<String> findRepeatingTokens() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Collection<? extends String> getRepeatedTokens() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ITokenIndex getTokenIndex(List<String> repeatedTokens) {
    // TODO Auto-generated method stub
    return null;
  }
}
