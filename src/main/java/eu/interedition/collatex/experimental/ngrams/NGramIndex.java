package eu.interedition.collatex.experimental.ngrams;

import java.util.List;

import com.google.common.collect.Lists;

public class NGramIndex {

  //TODO: make parameter a BiGramIndex class
  //TODO: make return type a NGramIndex
  static List<NGram> concatenateBiGramToNGram(final List<BiGram> biGramIndex) {
    final List<NGram> newNGrams;
    final NGram currentNGram = NGram.create(biGramIndex.remove(0)); // TODO: this can be dangerous; if there are no unique bigrams!
    for (final BiGram nextBiGram : biGramIndex) {
      //System.out.println(currentBiGram.getBeginPosition() + ":" + nextBiGram.getBeginPosition());
      currentNGram.add(nextBiGram);
      //   final Phrase newBigram = new Phrase(currentBiGram.getWitness(), currentBiGram.getFirstWord(), nextBiGram.getLastWord(), null);
      // newBiGrams.add(newBigram);
    }
    newNGrams = Lists.newArrayList(currentNGram);
    return newNGrams;
  }

}
