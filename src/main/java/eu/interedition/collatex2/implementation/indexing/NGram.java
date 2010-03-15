package eu.interedition.collatex2.implementation.indexing;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.INGram;
import eu.interedition.collatex2.interfaces.INormalizedToken;

public class NGram implements INGram {
  private final List<INormalizedToken> tokens;

  public NGram(final List<INormalizedToken> tokens) {
    this.tokens = tokens;
  }

  //  // TODO: rename parameter "remove" to bigram
  //  public static NGram create(final BiGram remove) {
  //    final List<NormalizedToken> tokens = Lists.newArrayList(remove.getFirstToken(), remove.getLastToken());
  //    return new NGram(tokens);
  //  }

  //  public static NGram create(final IWitness aa, final int startPosition, final int endPosition) {
  //    final List<NormalizedToken> tokens2 = aa.getTokens(startPosition, endPosition);
  //    return new NGram(tokens2);
  //  }

  //  // Note: not too pleased with this method! Not immutable!
  //  public void add(final BiGram nextBiGram) {
  //    tokens.add(nextBiGram.getLastToken());
  //  }

  public String getNormalized() {
    String replacementString = "";
    String divider = "";
    for (final INormalizedToken token : tokens) {
      replacementString += divider + token.getNormalized();
      divider = " ";
    }
    return replacementString;

  }

  // TODO: add test for defensive behavior!
  public INormalizedToken getFirstToken() {
    if (isEmpty()) {
      throw new RuntimeException("This ngram is empty!");
    }
    return tokens.get(0);
  }

  //TODO: make defensive and add test!
  public INormalizedToken getLastToken() {
    return tokens.get(tokens.size() - 1);
  }

  public boolean isEmpty() {
    return tokens.isEmpty();
  }

  public NGram trim() {
    final List<INormalizedToken> subList = tokens.subList(1, tokens.size() - 1);
    return new NGram(subList);
  }

  public static NGram create(final INormalizedToken token) {
    return new NGram(Lists.newArrayList(token));
  }

  @Override
  public int getBeginPosition() {
    return getFirstToken().getPosition();
  }

  @Override
  public int getEndPosition() {
    return getLastToken().getPosition();
  }

  @Override
  public String toString() {
    if (isEmpty()) {
      return "<empty>";
    }
    return getNormalized() + ":" + getBeginPosition() + ":" + getEndPosition();
  }

}
