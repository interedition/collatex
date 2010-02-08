package eu.interedition.collatex.experimental.ngrams;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

import eu.interedition.collatex.experimental.ngrams.data.Witness;

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
    final List<BiGram> biGrams1 = BiGrams.calculate(a);
    return new BiGramIndex(biGrams1);
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
