/*
 * Copyright (c) 2013 The Interedition Development Group.
 *
 * This file is part of CollateX.
 *
 * CollateX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CollateX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CollateX.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex.simple;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.Witness;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class SimpleWitness implements Iterable<Token>, Witness, Comparator<SimpleToken> {

  private final String sigil;
  private final List<Token> tokens = new ArrayList<Token>();

  public SimpleWitness(String sigil) {
    this.sigil = sigil;
  }

  public SimpleWitness(String sigil, String content) {
    this(sigil, content, SimplePatternTokenizer.BY_CHARACTERS_OR_WS_OR_PUNCT, SimpleTokenNormalizers.LC_TRIM_WS);
  }

  public SimpleWitness(String sigil,
                       String content,
                       Function<String, Iterable<String>> tokenizer,
                       Function<String, String> normalizer) {
    this(sigil);
    setTokenContents(tokenizer.apply(content), normalizer);
  }

  public List<Token> getTokens() {
    return tokens;
  }

  public void setTokenContents(Iterable<String> tokenContents, Function<String, String> normalizer) {
    final List<Token> tokens = Lists.newArrayListWithExpectedSize(Iterables.size(tokenContents));
    for (String content : tokenContents) {
      tokens.add(new SimpleToken(this, content, normalizer.apply(content)));
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

  @Override
  public int compare(SimpleToken o1, SimpleToken o2) {
    final int o1Index = tokens.indexOf(o1);
    final int o2Index = tokens.indexOf(o2);
    Preconditions.checkArgument(o1Index >= 0, o1);
    Preconditions.checkArgument(o2Index >= 0, o2);
    return (o1Index - o2Index);
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
