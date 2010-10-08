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

package eu.interedition.collatex2.interfaces;

import java.util.Collection;
import java.util.List;

/**
 * IWitness
 * 
 * Representation of a single textual witness
 *
 */
public interface IWitness extends ITokenContainer {

  // Note: This should return a non-modifiable container
  List<INormalizedToken> getTokens();

  IPhrase createPhrase(final int startPosition, final int endPosition);

  int size();

  String getSigil();

  //TODO: remove method!
  //TODO: Think about python bindings!
  List<String> findRepeatingTokens();

  ITokenIndex getTokenIndex(List<String> repeatedTokens);

  //TODO: remove method!
  Collection<? extends String> getRepeatedTokens();
}
