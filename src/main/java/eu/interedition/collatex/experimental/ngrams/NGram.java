package eu.interedition.collatex.experimental.ngrams;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.experimental.interfaces.IWitness;
import eu.interedition.collatex.experimental.ngrams.data.NormalizedToken;

public class NGram {
  private final List<NormalizedToken> tokens;

  public NGram(final List<NormalizedToken> tokens) {
    this.tokens = tokens;
  }

  // TODO rename parameter "remove" to bigram
  public static NGram create(final BiGram remove) {
    final List<NormalizedToken> tokens = Lists.newArrayList(remove.getFirstToken(), remove.getLastToken());
    return new NGram(tokens);
  }

  public static NGram create(final IWitness aa, final int startPosition, final int endPosition) {
    final List<NormalizedToken> tokens2 = aa.getTokens(startPosition, endPosition);
    return new NGram(tokens2);
  }

  // Note: not too pleased with this method! Not immutable!
  public void add(final BiGram nextBiGram) {
    tokens.add(nextBiGram.getLastToken());
  }

  public String getNormalized() {
    String replacementString = "";
    String divider = "";
    for (final NormalizedToken token : tokens) {
      replacementString += divider + token.getNormalized();
      divider = " ";
    }
    return replacementString;

  }

  // TODO add test for defensive behavior!
  public NormalizedToken getFirstToken() {
    if (isEmpty()) {
      throw new RuntimeException("This ngram is empty!");
    }
    return tokens.get(0);
  }

  //TODO make defensive and add test!
  public NormalizedToken getLastToken() {
    return tokens.get(tokens.size() - 1);
  }

  public boolean isEmpty() {
    return tokens.isEmpty();
  }

  public NGram trim() {
    final List<NormalizedToken> subList = tokens.subList(1, tokens.size() - 1);
    return new NGram(subList);
  }

}
