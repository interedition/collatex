package eu.interedition.collatex.experimental.ngrams;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

public class NGramIndex {

  //TODO: rename to create?
  //TODO: make return type a NGramIndex
  static List<NGram> concatenateBiGramToNGram(final BiGramIndex biGramIndex) {
    final List<BiGram> biGrams = Lists.newArrayList(biGramIndex);
    if (biGrams.isEmpty()) {
      return Collections.emptyList();
    }
    final List<NGram> newNGrams = Lists.newArrayList();
    NGram currentNGram = NGram.create(biGrams.remove(0));
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
