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

import com.google.common.collect.Maps;
import eu.interedition.collatex.interfaces.INormalizedToken;
import eu.interedition.collatex.interfaces.IToken;
import eu.interedition.collatex.interfaces.IWitness;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Witness implements Iterable<INormalizedToken>, IWitness {
  public static final Witness SUPERBASE = new Witness("");

  private final String sigil;
  protected List<INormalizedToken> tokens = new ArrayList<INormalizedToken>();
  private final Map<INormalizedToken, INormalizedToken> relations = Maps.newLinkedHashMap();

  public Witness(final String sigil) {
    this.sigil = sigil;
  }

  protected void prepareTokens() {
    relations.clear();
    INormalizedToken previous = NormalizedToken.START;
    for (INormalizedToken token : tokens) {
      relations.put(previous, token);
      previous = token;
    }
    relations.put(previous, NormalizedToken.END);
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

  // TODO check whether iterator.remove() throws exception!
  @Override
  public Iterator<INormalizedToken> iterator() {
    return tokens.iterator();
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

  @Override
  public int compareTo(IWitness o) {
    return sigil.compareTo(o.getSigil());
  }
}
