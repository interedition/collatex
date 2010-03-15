package eu.interedition.collatex2.implementation.tokenization;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.input.NormalizedToken;
import eu.interedition.collatex2.implementation.input.NormalizedWitness;
import eu.interedition.collatex2.implementation.input.Token;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class NormalizedWitnessBuilder {

  public static IWitness create(final String sigil, final String words) {
    final List<Token> tokens = tokenize(sigil, words);
    final List<INormalizedToken> normalizeds = normalize(tokens);
    final NormalizedWitness result = new NormalizedWitness(sigil, normalizeds);
    return result;
  }

  private static List<Token> tokenize(final String sigil, final String words) {
    final Tokenizer tokenizer = new Tokenizer(sigil, words);
    final List<Token> tokens = Lists.newArrayList();
    while (tokenizer.hasNext()) {
      final Token next = tokenizer.nextToken();
      tokens.add(next);
    }
    return tokens;
  }

  private static List<INormalizedToken> normalize(final List<Token> tokens) {
    final List<INormalizedToken> normalizeds = Lists.newArrayList();
    for (final Token token : tokens) {
      final INormalizedToken normalizedT = NormalizedToken.normalize(token);
      normalizeds.add(normalizedT);
    }
    return normalizeds;
  }

}
