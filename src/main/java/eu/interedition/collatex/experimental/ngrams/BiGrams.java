package eu.interedition.collatex.experimental.ngrams;

import java.util.List;

import eu.interedition.collatex.input.Phrase;
import eu.interedition.collatex.interfaces.IWitness;
import eu.interedition.collatex.interfaces.WitnessF;

public class BiGrams {

  public static List<Subsegment2> getOverlappingBiGrams(final IWitness a, final IWitness b) {
    final BiGramIndexGroup group = BiGramIndexGroup.create(a, b);
    return group.getOverlap();
  }

  public static List<BiGram> getOverlappingBiGramsForWitnessA(final IWitness a, final IWitness b) {
    final BiGramIndexGroup group = BiGramIndexGroup.create(a, b);
    return group.getOverlappingBiGramsForWitnessA();
  }

  // TODO move this to the BiGramIndexGroup!
  public static List<NGram> getUniqueBiGramsForWitnessA(final IWitness a, final IWitness b) {
    final BiGramIndexGroup group = BiGramIndexGroup.create(a, b);
    return group.getUniqueNGramsForWitnessA();
  }

  // TODO this method is not finished!
  // TODO does not work right for multiple groups of bigrams
  public static List<Phrase> getLongestUniquePiecesForWitnessA(final WitnessF a, final WitnessF b) {
    throw new UnsupportedOperationException("NOT YET IMPLEMENTED!");

  }

  // TODO this method is not finished!
  public static List<Phrase> getLongestUniquePiecesForWitnessB(final WitnessF a, final WitnessF b) {
    throw new UnsupportedOperationException("NOT YET IMPLEMENTED!");
    //    final List<Phrase> uniqueBiGramsForWitnessB = getUniqueBiGramsForWitnessB(a, b);
    //    return uniqueBiGramsForWitnessB;
  }

}
