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

import com.google.common.collect.Maps;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.Token;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SimpleWitness implements Iterable<Token>, Witness {
  public static final SimpleWitness SUPERBASE = new SimpleWitness("");

  private final String sigil;
  protected List<Token> tokens = new ArrayList<Token>();
  private final Map<Token, Token> relations = Maps.newLinkedHashMap();

  public SimpleWitness(final String sigil) {
    this.sigil = sigil;
  }

  protected void prepareTokens() {
    relations.clear();
    Token previous = SimpleToken.START;
    for (Token token : tokens) {
      relations.put(previous, token);
      previous = token;
    }
    relations.put(previous, SimpleToken.END);
  }

  // Note: not pleased with this method! implement Iterable!
  @Override
  public List<Token> getTokens() {
    return tokens;
  }

  public void setTokens(List<Token> tokens) {
    this.tokens = tokens;
    prepareTokens();
  }

  @Override
  public String getSigil() {
    return sigil;
  }

  // TODO check whether iterator.remove() throws exception!
  @Override
  public Iterator<Token> iterator() {
    return tokens.iterator();
  }

  @Override
  public String toString() {
    return getSigil();
  }

  @Override
  public boolean isNear(Token a, Token b) {
    if (!relations.containsKey(a)) {
      throw new RuntimeException("Error; "+a+" is an unknown token! "+a.getClass());
    }
    Token other = relations.get(a);
    return other.equals(b);
  }

  @Override
  public int compareTo(Witness o) {
    return sigil.compareTo(o.getSigil());
  }
}
