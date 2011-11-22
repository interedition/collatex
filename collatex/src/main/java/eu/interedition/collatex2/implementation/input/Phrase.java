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

package eu.interedition.collatex2.implementation.input;

import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;

public class Phrase implements IPhrase {
  private final List<INormalizedToken> tokens;

  public static final Comparator<IPhrase> PHRASECOMPARATOR = new Comparator<IPhrase>() {
    @Override
    public int compare(final IPhrase p1, final IPhrase p2) {
      return p1.compareTo(p2);
    }
  };

  public Phrase(final List<INormalizedToken> tokens1) {
    this.tokens = tokens1;
  }

  //  // TODO rename parameter "remove" to bigram
  //  public static Phrase create(final BiGram remove) {
  //    final List<NormalizedToken> tokens = Lists.newArrayList(remove.getFirstToken(), remove.getLastToken());
  //    return new Phrase(tokens);
  //  }

  //  public static Phrase create(final IWitness aa, final int startPosition, final int endPosition) {
  //    final List<NormalizedToken> tokens2 = aa.getTokens(startPosition, endPosition);
  //    return new Phrase(tokens2);
  //  }

  //  // Note: not too pleased with this method! Not immutable!
  //  public void add(final BiGram nextBiGram) {
  //    tokens.add(nextBiGram.getLastToken());
  //  }

  @Override
  public String getNormalized() {
    final StringBuilder normalized = new StringBuilder();
    for (final INormalizedToken token : tokens) {
      normalized.append(token.getNormalized()).append(token.getTrailingWhitespace());
    }
    return normalized.toString();

  }

  //TODO: add escaping!
  @Override
  public String getContent() {
    final StringBuilder content = new StringBuilder();
    for (final INormalizedToken token : tokens) {
      content.append(token.getContent()).append(token.getTrailingWhitespace());
    }
    return content.toString();
  }

  // TODO add test for defensive behavior!
  @Override
  public INormalizedToken getFirstToken() {
    if (isEmpty()) {
      throw new RuntimeException("This ngram is empty!");
    }
    return tokens.get(0);
  }

  //TODO make defensive and add test!
  @Override
  public INormalizedToken getLastToken() {
    return tokens.get(tokens.size() - 1);
  }

  @Override
  public boolean isEmpty() {
    return tokens.isEmpty();
  }

  public Phrase trim() {
    final List<INormalizedToken> subList = tokens.subList(1, tokens.size() - 1);
    return new Phrase(subList);
  }

  public static Phrase create(final INormalizedToken token) {
    return new Phrase(Lists.newArrayList(token));
  }

  @Override
  public String toString() {
    if (isEmpty()) {
      return "<empty>";
    }
    return getContent(); /*getNormalized() + ":" + getBeginPosition() + ":" + getEndPosition();*/
  }

  @Override
  public List<INormalizedToken> getTokens() {
    return tokens;
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof Phrase)) {
      return false;
    }
    return tokens.equals(((Phrase) obj).getTokens());
  }

  @Override
  public int size() {
    return tokens.size();
  }

  @Override
  public int compareTo(final IPhrase other) {
//    final int beginDelta = getBeginPosition() - other.getBeginPosition();
//    if (beginDelta != 0) {
//      return beginDelta;
//    }
//    final int endDelta = getEndPosition() - other.getEndPosition();
//    if (endDelta != 0) {
//      return endDelta;
//    }
    final int sizeDelta = getTokens().size() - other.getTokens().size();
    return sizeDelta;
  }

  @Override
  public IPhrase createSubPhrase(final int startIndex, final int endIndex) {
    return new Phrase(tokens.subList(startIndex - 1, endIndex));
  }

  @Override
  public void addTokenToRight(final INormalizedToken token) {
    tokens.add(token);
  }

  @Override
  public void addTokenToLeft(final INormalizedToken token) {
    tokens.add(0, token);
  }

}
