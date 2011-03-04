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

package eu.interedition.collatex2.implementation.vg_alignment;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.ITokenMatch;

public class TokenMatch implements ITokenMatch {
  private final INormalizedToken baseToken;
  private final INormalizedToken witnessToken;

  public TokenMatch(INormalizedToken baseToken, INormalizedToken witnessToken) {
    this.baseToken = baseToken;
    this.witnessToken = witnessToken;
  }

  @Override
  public INormalizedToken getBaseToken() {
   return baseToken;
  }

  @Override
  public INormalizedToken getWitnessToken() {
    return witnessToken;
  }
  
  @Override
  public String toString() {
    return witnessToken.toString()+" -> "+getBaseToken();
  }

  @Override
  public String getNormalized() {
    return witnessToken.getNormalized();
  }

  @Override
  public INormalizedToken getTableToken() {
    return baseToken;
  }

  @Override
  public INormalizedToken getTokenA() {
    return witnessToken;
  }

  @Override
  public INormalizedToken getTokenB() {
    return baseToken;
  }
}
