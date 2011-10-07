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

package eu.interedition.collatex2.implementation.containers.witness;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import eu.interedition.collatex2.implementation.input.Phrase;
import eu.interedition.collatex2.implementation.vg_alignment.EndToken;
import eu.interedition.collatex2.implementation.vg_alignment.StartToken;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class Witness implements Iterable<INormalizedToken>, IWitness {
  private String sigil;
  protected List<INormalizedToken> tokens;
  private Map<INormalizedToken, INormalizedToken> relations;

  public Witness() {}

  public Witness(final String sigil) {
    this(sigil, new ArrayList<INormalizedToken>());
  }

  public Witness(final String sigil, final List<INormalizedToken> tokens) {
    this.sigil = sigil;
    this.tokens = tokens;
    prepareTokens();
  }

  protected void prepareTokens() {
    relations = Maps.newLinkedHashMap();
    INormalizedToken previous = new StartToken();
    for (INormalizedToken token : tokens) {
      relations.put(previous, token);
      previous = token;
    }
    relations.put(previous, new EndToken(tokens.size()+1));
  }

  // Note: not pleased with this method! implement Iterable!
  @Override
  public List<INormalizedToken> getTokens() {
    return tokens;
  }

  public void setTokens(List<INormalizedToken> tokens) {
    this.tokens = tokens;
    prepareTokens();
  }

  @Override
  public String getSigil() {
    return sigil;
  }

  public void setSigil(String sigil) {
    this.sigil = sigil;
  }

  // TODO check whether iterator.remove() throws exception!
  @Override
  public Iterator<INormalizedToken> iterator() {
    return tokens.iterator();
  }

  // NOTE: this method is not on the IWitness interface
  public IPhrase createPhrase(final int startPosition, final int endPosition) {
    // TODO this problemCase shouldn't occur
    final boolean problemCase = (startPosition - 1 > endPosition);
    final List<INormalizedToken> subList = problemCase ? new ArrayList<INormalizedToken>() : tokens.subList(startPosition - 1, endPosition);
    return new Phrase(subList);
  }

  @Override
  public int size() {
    return tokens.size();
  }

  @Override
  public String toString() {
    return getSigil();
  }

  @Override
  public boolean isNear(IToken a, IToken b) {
    if (!relations.containsKey(a)) {
      throw new RuntimeException("Error; "+a+" is an unknown token! "+a.getClass());
    }
    INormalizedToken other = relations.get(a);
    return other.equals(b);
  }

  @Override
  public Iterator<INormalizedToken> tokenIterator() {
    return iterator();
  }
}
