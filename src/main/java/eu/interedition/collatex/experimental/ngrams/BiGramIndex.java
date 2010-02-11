package eu.interedition.collatex.experimental.ngrams;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex.experimental.ngrams.data.NormalizedToken;
import eu.interedition.collatex.experimental.ngrams.data.NormalizedWitness;
import eu.interedition.collatex.experimental.ngrams.data.SpecialToken;
import eu.interedition.collatex.experimental.ngrams.data.Witness;
import eu.interedition.collatex.experimental.ngrams.tokenization.NormalizedWitnessBuilder;

public class BiGramIndex {

  private final List<BiGram> biGrams1;

  public BiGramIndex(final List<BiGram> biGrams1) {
    this.biGrams1 = biGrams1;
  }

  // TODO: remove!
  public Map normalize() {
    final Map<String, BiGram> biGramMapped1 = normalize(biGrams1);
    return biGramMapped1;
  }

  public static BiGramIndex create(final Witness a) {
    final List<BiGram> biGrams1 = BiGramIndex.calculate(a);
    return new BiGramIndex(biGrams1);
  }

  // TODO: replace calls to this method with calls to create!
  // TODO: make this method private
  public static List<BiGram> calculate(final Witness a) {
    final NormalizedWitness aa = NormalizedWitnessBuilder.create(a);
    final List<NormalizedToken> tokens = aa.getTokens();
    final List<NormalizedToken> tokensTodo = Lists.newArrayList(tokens);
    tokensTodo.add(new SpecialToken(a.getSigil(), "#", tokens.size() + 1));
    NormalizedToken previous = new SpecialToken(a.getSigil(), "#", 0);
    final List<BiGram> bigrams = Lists.newArrayList();
    for (final NormalizedToken next : tokensTodo) {
      final BiGram tuple = new BiGram(previous, next);
      bigrams.add(tuple);
      previous = next;
    }
    return bigrams;
  }

  // TODO: integrate the two static functions into one!
  private static Map<String, BiGram> normalize(final List<BiGram> ngrams) {
    final Map<String, BiGram> normalized = Maps.newLinkedHashMap();
    for (final BiGram ngram : ngrams) {
      normalized.put(ngram.getNormalized(), ngram);
    }
    return normalized;
  }

  public Set<String> keys() {
    final Map<String, BiGram> normalize = normalize(biGrams1);
    return normalize.keySet();
  }

  public BiGram get(final String key) {
    final Map<String, BiGram> normalize = normalize(biGrams1);
    return normalize.get(key);
  }

}