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

import eu.interedition.collatex.Token;
import eu.interedition.collatex.Witness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimpleWitness implements Iterable<Token>, Witness, Comparator<SimpleToken> {

  private final String sigil;
  private final List<Token> tokens = new ArrayList<>();

  public SimpleWitness(String sigil) {
    this.sigil = sigil;
  }

  public SimpleWitness(String sigil, String content) {
    this(sigil, content, SimplePatternTokenizer.BY_WS_OR_PUNCT, SimpleTokenNormalizers.LC_TRIM_WS);
  }

  public SimpleWitness(String sigil,
                       String content,
                       Function<String, Stream<String>> tokenizer,
                       Function<String, String> normalizer) {
    this(sigil);
    setTokenContents(tokenizer.apply(content), normalizer);
  }

  public List<Token> getTokens() {
    return tokens;
  }

  public void setTokenContents(Stream<String> tokenContents, Function<String, String> normalizer) {
    setTokens(tokenContents.map(content -> new SimpleToken(SimpleWitness.this, content, normalizer.apply(content))).collect(Collectors.toList()));
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
    return Collections.unmodifiableList(tokens).iterator();
  }

  @Override
  public String toString() {
    return getSigil();
  }

  @Override
  public int compare(SimpleToken o1, SimpleToken o2) {
    final int o1Index = tokens.indexOf(o1);
    final int o2Index = tokens.indexOf(o2);
    if (o1Index < 0) {
      throw new IllegalArgumentException(o1.toString());
    }
    if (o2Index < 0) {
      throw new IllegalArgumentException();
    }
    return (o1Index - o2Index);
  }

  public static final Pattern PUNCT = Pattern.compile("\\p{Punct}");

  public static final Function<String, String> TOKEN_NORMALIZER = input -> {
    final String normalized = PUNCT.matcher(input.trim().toLowerCase()).replaceAll("");
    return (normalized == null || normalized.length() == 0 ? input : normalized);
  };

}
