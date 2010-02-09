package eu.interedition.collatex.experimental.ngrams;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.experimental.ngrams.data.NormalizedToken;
import eu.interedition.collatex.experimental.ngrams.data.NormalizedWitness;
import eu.interedition.collatex.experimental.ngrams.data.SpecialToken;
import eu.interedition.collatex.experimental.ngrams.data.Witness;
import eu.interedition.collatex.experimental.ngrams.tokenization.NormalizedWitnessBuilder;
import eu.interedition.collatex.input.Phrase;

public class BiGrams {

  // TODO: shouldn't this return value become an instance of bigramindex?
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

  public static List<Subsegment2> getOverlappingBiGrams(final Witness a, final Witness b) {
    final BiGramIndexGroup group = BiGramIndexGroup.create(a, b);
    return group.getOverlap();
  }

  // TODO: move this to the BiGramIndexGroup!
  // TODO: maybe this method should return a BiGramIndex!
  public static List<Subsegment2> getUniqueBiGramsForWitnessA(final Witness a, final Witness b) {
    final BiGramIndexGroup group = BiGramIndexGroup.create(a, b);
    return group.getUniqueBiGramsForWitnessA();
  }

  public static List<BiGram> getUniqueBiGramsForWitnessB(final Witness a, final Witness b) {
    final BiGramIndexGroup group = BiGramIndexGroup.create(a, b);
    return group.getUniqueBiGramsForWitnessB();
  }

  // TODO: this method is not finished!
  // TODO: does not work right for multiple groups of bigrams
  public static List<Phrase> getLongestUniquePiecesForWitnessA(final Witness a, final Witness b) {
    throw new UnsupportedOperationException("NOT YET IMPLEMENTED!");

    //    final List<Phrase> uniqueBiGramsForWitnessA = getUniqueBiGramsForWitnessA(a, b);
    //    final List<Phrase> newBiGrams = Lists.newArrayList();
    //    final Phrase currentBiGram = uniqueBiGramsForWitnessA.remove(0); // TODO: this can be dangerous; if there are no unique bigrams!
    //    for (final Phrase nextBiGram : uniqueBiGramsForWitnessA) {
    //      System.out.println(currentBiGram.getBeginPosition() + ":" + nextBiGram.getBeginPosition());
    //      final Phrase newBigram = new Phrase(currentBiGram.getWitness(), currentBiGram.getFirstWord(), nextBiGram.getLastWord(), null);
    //      newBiGrams.add(newBigram);
    //    }
    //    return newBiGrams;
  }

  // TODO: this method is not finished!
  public static List<Phrase> getLongestUniquePiecesForWitnessB(final Witness a, final Witness b) {
    throw new UnsupportedOperationException("NOT YET IMPLEMENTED!");
    //    final List<Phrase> uniqueBiGramsForWitnessB = getUniqueBiGramsForWitnessB(a, b);
    //    return uniqueBiGramsForWitnessB;
  }

}
