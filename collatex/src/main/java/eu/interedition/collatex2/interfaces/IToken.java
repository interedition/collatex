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

/**
 * The basic Token interface. The smallest piece of the text to align. It has the minimal number of fields: content (and sigil) 
 * TODO: Move to IWitness and make the getSigil method not needed anymore?
 * TODO: Should getPosition() be on the interface? 
 * 
 */
public interface IToken {
  int getPosition();

  String getSigil();
/**
 * The content stream token text
 * @return the text of the token.
 */
  String getContent();


}
