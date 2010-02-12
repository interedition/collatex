package eu.interedition.collatex.experimental.ngrams;

import java.util.List;

import com.google.common.collect.Lists;

public class NGramIndex {

  //TODO: rename to create?
  //TODO: make return type a NGramIndex
  static List<NGram> concatenateBiGramToNGram(final BiGramIndex biGramIndex) {
    final List<BiGram> biGrams = Lists.newArrayList(biGramIndex);
    final List<NGram> newNGrams = Lists.newArrayList();
    NGram currentNGram = NGram.create(biGrams.remove(0)); // TODO: this can be dangerous; if there are no unique bigrams!
    newNGrams.add(currentNGram);
    for (final BiGram nextBiGram : biGrams) {
      if (nextBiGram.getFirstToken().getPosition() - currentNGram.getLastToken().getPosition() > 1) {
        currentNGram = NGram.create(nextBiGram);
        newNGrams.add(currentNGram);
      } else {
        currentNGram.add(nextBiGram);
      }
    }
    return newNGrams;
  }
}
