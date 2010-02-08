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
    throw new RuntimeException("Not yet implemented!");
    //    final List<Subsegment2> biGrams1 = calculate(a);
    //    final List<Subsegment2> biGrams2 = calculate(b);
    //    final Map<String, Subsegment2> biGramMapped1 = normalize(biGrams1);
    //    final Map<String, Subsegment2> biGramMapped2 = normalize(biGrams2);
    //    final List<String> uniqueBigramsForWitnessANormalized = Lists.newArrayList(biGramMapped1.keySet());
    //    uniqueBigramsForWitnessANormalized.removeAll(biGramMapped2.keySet());
    //    System.out.println(uniqueBigramsForWitnessANormalized);
    //    final List<Subsegment2> subsegments = Lists.newArrayList();
    //    for (final String normalized : uniqueBigramsForWitnessANormalized) {
    //      final Subsegment2 phrase1 = biGramMapped1.get(normalized);
    //      subsegments.add(phrase1);
    //    }
    //    return subsegments;
    //    final List<Subsegment2> overlappingBiGrams = getOverlappingBiGrams(a, b);
    //    // hmm hier heb ik weer de bigrams nodig van Witness A; dat wordt dan wel dubbel berekend.. zucht
    //    final List<Phrase> biGramsForWitnessA = calculate(a);
    //    final List<Phrase> uniqueBiGramsForWitnessA = Lists.newArrayList();
    //    uniqueBiGramsForWitnessA.addAll(biGramsForWitnessA);
    //
    //    for (final Subsegment2 overlappingBiGram : overlappingBiGrams) {
    //      final Phrase overlappingPhraseInWitnessA = overlappingBiGram.getPhraseFor(a.getFirstSegment().getWitnessId());
    //      // TODO: this is here because Phrase has no equals!
    //      // TODO: should be toTest.getNormalized!;
    //      // TODO: this is inefficient!
    //      for (final Phrase toTest : biGramsForWitnessA) {
    //        if (toTest.getOriginal().equals(overlappingPhraseInWitnessA.getOriginal())) {
    //          uniqueBiGramsForWitnessA.remove(toTest);
    //        }
    //      }
    //    }
    //    return uniqueBiGramsForWitnessA;
  }

  // TODO: method who are doing almost the same thing! That should not be necessary!
  public static List<Subsegment2> getUniqueBiGramsForWitnessB(final Witness a, final Witness b) {
    throw new UnsupportedOperationException("NOT YET IMPLEMENTED!");
    //    final List<Subsegment2> biGrams1 = calculate(a);
    //    final List<Subsegment2> biGrams2 = calculate(b);
    //    final Map<String, Subsegment2> biGramMapped1 = normalize(biGrams1);
    //    final Map<String, Subsegment2> biGramMapped2 = normalize(biGrams2);
    //    // Until here is the exact same stuff as the other method!
    //    final List<String> result = Lists.newArrayList(biGramMapped2.keySet());
    //    result.removeAll(biGramMapped1.keySet());
    //    System.out.println(result);
    //    // The next part is also the same! (only the map were it comes from is different!
    //    final List<Subsegment2> subsegments = Lists.newArrayList();
    //    for (final String normalized : result) {
    //      final Subsegment2 phrase1 = biGramMapped2.get(normalized);
    //      subsegments.add(phrase1);
    //    }
    //    return subsegments;
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
