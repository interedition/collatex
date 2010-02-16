package eu.interedition.collatex.experimental.ngrams;

import java.util.List;

import eu.interedition.collatex.experimental.ngrams.data.Witness;
import eu.interedition.collatex.input.Phrase;

public class BiGrams {

  public static List<Subsegment2> getOverlappingBiGrams(final Witness a, final Witness b) {
    final BiGramIndexGroup group = BiGramIndexGroup.create(a, b);
    return group.getOverlap();
  }

  public static List<BiGram> getOverlappingBiGramsForWitnessA(final Witness a, final Witness b) {
    final BiGramIndexGroup group = BiGramIndexGroup.create(a, b);
    return group.getOverlappingBiGramsForWitnessA();
  }

  // TODO: move this to the BiGramIndexGroup!
  public static List<NGram> getUniqueBiGramsForWitnessA(final Witness a, final Witness b) {
    final BiGramIndexGroup group = BiGramIndexGroup.create(a, b);
    return group.getUniqueNGramsForWitnessA();
  }

  public static List<BiGram> getUniqueBiGramsForWitnessB(final Witness a, final Witness b) {
    final BiGramIndexGroup group = BiGramIndexGroup.create(a, b);
    return group.getUniqueBiGramsForWitnessB();
  }

  // TODO: this method is not finished!
  // TODO: does not work right for multiple groups of bigrams
  public static List<Phrase> getLongestUniquePiecesForWitnessA(final Witness a, final Witness b) {
    throw new UnsupportedOperationException("NOT YET IMPLEMENTED!");

  }

  // TODO: this method is not finished!
  public static List<Phrase> getLongestUniquePiecesForWitnessB(final Witness a, final Witness b) {
    throw new UnsupportedOperationException("NOT YET IMPLEMENTED!");
    //    final List<Phrase> uniqueBiGramsForWitnessB = getUniqueBiGramsForWitnessB(a, b);
    //    return uniqueBiGramsForWitnessB;
  }

  public static List<NGram> getUniqueNGramsForWitnessB(final Witness a, final Witness b) {
    final BiGramIndexGroup group = BiGramIndexGroup.create(a, b);
    return group.getUniqueNGramsForWitnessB();
  }

}
