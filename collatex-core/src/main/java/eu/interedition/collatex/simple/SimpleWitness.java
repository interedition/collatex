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

package eu.interedition.collatex.simple;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.Witness;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class SimpleWitness implements Iterable<Token>, Witness {

  private final String sigil;
  private final List<Token> tokens = new ArrayList<Token>();

  public SimpleWitness(String sigil) {
    this.sigil = sigil;
  }

  public SimpleWitness(String sigil, String content, Function<String, List<String>> tokenizer) {
    this(sigil);
    setTokenContents(tokenizer.apply(content));
  }

  public List<Token> getTokens() {
    return tokens;
  }

  public void setTokenContents(List<String> tokenContents) {
    final List<Token> tokens = Lists.newArrayListWithExpectedSize(tokenContents.size());
    for (String content : tokenContents) {
      tokens.add(new SimpleToken(this, tokens.size(), content, TOKEN_NORMALIZER.apply(content)));
    }
    setTokens(tokens);
  }

  public void setTokens(List<Token> tokens) {
    this.tokens.clear();
    this.tokens.addAll(tokens);
  }

  @Override
  public String getSigil() {
    return sigil;
  }

  @Override
  public Iterator<Token> iterator() {
    return Iterators.unmodifiableIterator(tokens.iterator());
  }

  @Override
  public String toString() {
    return getSigil();
  }

  public static final Pattern PUNCT = Pattern.compile("\\p{Punct}");

  public static final Function<String, String> TOKEN_NORMALIZER = new Function<String, String>() {
    @Override
    public String apply(String input) {
      final String normalized = PUNCT.matcher(input.trim().toLowerCase()).replaceAll("");
      return (normalized == null || normalized.length() == 0 ? input : normalized);
    }
  };
}
