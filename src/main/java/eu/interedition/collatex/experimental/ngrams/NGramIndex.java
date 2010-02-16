package eu.interedition.collatex.experimental.ngrams;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

public class NGramIndex {

  //TODO: make return type a NGramIndex
  static List<NGram> concatenateBiGramToNGram(final BiGramIndex biGramIndex) {
    // TODO: not making a copy here might be dangerous!
    final List<BiGram> biGrams = biGramIndex.getBiGrams();
    if (biGrams.isEmpty()) {
      return Collections.emptyList();
    }
    final List<NGram> newNGrams;
    final NGram currentNGram = NGram.create(biGrams.remove(0));
    for (final BiGram nextBiGram : biGrams) {
      //System.out.println(currentBiGram.getBeginPosition() + ":" + nextBiGram.getBeginPosition());
      currentNGram.add(nextBiGram);
      //   final Phrase newBigram = new Phrase(currentBiGram.getWitness(), currentBiGram.getFirstWord(), nextBiGram.getLastWord(), null);
      // newBiGrams.add(newBigram);
    }
    newNGrams = Lists.newArrayList(currentNGram);
    return newNGrams;
  }

}
