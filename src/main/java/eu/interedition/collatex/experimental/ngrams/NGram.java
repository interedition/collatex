package eu.interedition.collatex.experimental.ngrams;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.experimental.ngrams.data.NormalizedToken;
import eu.interedition.collatex.experimental.ngrams.data.NormalizedWitness;

public class NGram {
  private final List<NormalizedToken> tokens;

  public NGram(final List<NormalizedToken> tokens) {
    this.tokens = tokens;
  }

  // TODO: rename parameter "remove" to bigram
  public static NGram create(final BiGram remove) {
    final List<NormalizedToken> tokens = Lists.newArrayList(remove.getFirstToken(), remove.getLastToken());
    return new NGram(tokens);
  }

  public static NGram create(final NormalizedWitness aa, final int startPosition, final int endPosition) {
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

  public NormalizedToken getFirstToken() {
    return tokens.get(0);
  }

  public NormalizedToken getLastToken() {
    return tokens.get(tokens.size() - 1);
  }

}
