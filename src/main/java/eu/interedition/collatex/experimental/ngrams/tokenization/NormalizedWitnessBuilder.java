package eu.interedition.collatex.experimental.ngrams.tokenization;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.experimental.ngrams.data.NormalizedToken;
import eu.interedition.collatex.experimental.ngrams.data.NormalizedWitness;
import eu.interedition.collatex.experimental.ngrams.data.Token;
import eu.interedition.collatex.experimental.ngrams.data.Witness;

public class NormalizedWitnessBuilder {

  public static NormalizedWitness create(final Witness witness) {
    final List<Token> tokens = tokenize(witness);
    final List<NormalizedToken> normalizeds = normalize(tokens);
    final NormalizedWitness result = new NormalizedWitness(witness.getSigil(), normalizeds);
    return result;
  }

  private static List<Token> tokenize(final Witness witness) {
    final Tokenizer tokenizer = new Tokenizer(witness);
    final List<Token> tokens = Lists.newArrayList();
    while (tokenizer.hasNext()) {
      final Token next = tokenizer.nextToken();
      tokens.add(next);
    }
    return tokens;
  }

  private static List<NormalizedToken> normalize(final List<Token> tokens) {
    final List<NormalizedToken> normalizeds = Lists.newArrayList();
    for (final Token token : tokens) {
      final NormalizedToken normalizedT = NormalizedToken.normalize(token);
      normalizeds.add(normalizedT);
    }
    return normalizeds;
  }

}
