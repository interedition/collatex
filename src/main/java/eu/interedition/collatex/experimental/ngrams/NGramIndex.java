package eu.interedition.collatex.experimental.ngrams;

import java.util.List;

import com.google.common.collect.Lists;

public class NGramIndex {

  //TODO: make return type a NGramIndex
  static List<NGram> concatenateBiGramToNGram(final BiGramIndex biGramIndex) {
    final List<BiGram> biGrams = Lists.newArrayList(biGramIndex);
    final List<NGram> newNGrams;
    final NGram currentNGram = NGram.create(biGrams.remove(0)); // TODO: this can be dangerous; if there are no unique bigrams!
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
