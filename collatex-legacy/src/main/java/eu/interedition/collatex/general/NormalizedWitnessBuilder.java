package eu.interedition.collatex.general;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.experimental.ngrams.data.NormalizedToken;
import eu.interedition.collatex.experimental.ngrams.data.NormalizedWitness;
import eu.interedition.collatex.experimental.ngrams.data.Token;
import eu.interedition.collatex.experimental.ngrams.tokenization.Tokenizer;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class NormalizedWitnessBuilder {

  public static IWitness create(final String sigil, final String words) {
    final List<Token> tokens = tokenize(sigil, words);
    final List<INormalizedToken> normalizeds = normalize(tokens);
    final IWitness result = new NormalizedWitness(sigil, normalizeds);
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
