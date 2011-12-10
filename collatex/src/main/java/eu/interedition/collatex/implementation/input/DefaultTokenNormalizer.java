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

import eu.interedition.collatex.interfaces.ITokenNormalizer;

import java.util.regex.Pattern;

/**
 * Default implementation of a token normalizer.
 * 
 * <p>Lowercases the token and strips punctuation</p>
 *
 */
public class DefaultTokenNormalizer implements ITokenNormalizer {
  private final static Pattern PUNCT = Pattern.compile("\\p{Punct}");
  
  @Override
  public String apply(String input) {
    final String normalized = PUNCT.matcher(input.trim().toLowerCase()).replaceAll("");
    return (normalized == null || normalized.length() == 0 ? input : normalized);
  }
}
