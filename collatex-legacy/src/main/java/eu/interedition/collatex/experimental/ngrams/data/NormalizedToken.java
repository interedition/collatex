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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.interedition.collatex2.interfaces.INormalizedToken;

public class NormalizedToken extends Token implements INormalizedToken {
  private final String normalized;
  private final static Pattern PUNCT = Pattern.compile("\\p{Punct}");

  public NormalizedToken(final String sigil, final String content, final int position, final String normalized) {
    super(sigil, content, position);
    this.normalized = normalized;
  }

  public String getNormalized() {
    return normalized;
  }

  public static INormalizedToken normalize(final Token token) {
    final String content = token.getContent();
    String normalized = content.toLowerCase();
    final Matcher matcher = PUNCT.matcher(normalized);
    final boolean find = matcher.find();
    if (find) {
      normalized = matcher.replaceAll("");
    }
    final NormalizedToken normalizedT = create(token, normalized);
    return normalizedT;
  }

  public static NormalizedToken create(final Token token, final String normalized) {
    return new NormalizedToken(token.getSigil(), token.getContent(), token.getPosition(), normalized);
  }

}
