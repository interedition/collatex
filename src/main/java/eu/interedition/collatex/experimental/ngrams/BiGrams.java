package eu.interedition.collatex.experimental.ngrams;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex.input.Phrase;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;

public class BiGrams {

  public static List<Subsegment2> calculate(final Witness a) {
    // Note: getFirstSegment is not so nice; extra indirection
    final List<Word> words = a.getFirstSegment().getWords();
    final List<Word> wordsTodo = Lists.newArrayList();
    wordsTodo.addAll(words);
    wordsTodo.add(new SpecialWord(a.getFirstSegment().id, "#", words.size() + 1));
    Word previous = new SpecialWord(a.getFirstSegment().id, "#", 0);
    final List<Subsegment2> bigrams = Lists.newArrayList();
    for (final Word next : wordsTodo) {
      final WordsTuple tuple = new WordsTuple(previous, next);
      final Subsegment2 bigram = new Subsegment2(tuple.getNormalized(), tuple);
      bigrams.add(bigram);
      previous = next;
    }
    return bigrams;
  }

  public static List<Subsegment2> getOverlappingBiGrams(final Witness a, final Witness b) {
    throw new UnsupportedOperationException("NOT YET IMPLEMENTED!");
    //    final List<Phrase> biGrams1 = calculate(a);
    //    final List<Phrase> biGrams2 = calculate(b);
    //    final Map<String, Phrase> biGramMapped1 = normalize(biGrams1);
    //    final Map<String, Phrase> biGramMapped2 = normalize(biGrams2);
    //    final Set<String> union = biGramMapped1.keySet();
    //    union.retainAll(biGramMapped2.keySet());
    //    final List<Subsegment2> subsegments = Lists.newArrayList();
    //    for (final String normalized : union) {
    //      final Phrase phrase1 = biGramMapped1.get(normalized);
    //      final Phrase phrase2 = biGramMapped2.get(normalized);
    //      final Subsegment2 subsegment = new Subsegment2(normalized, phrase1, phrase2);
    //      subsegments.add(subsegment);
    //    }
    //    return subsegments;
  }

  private static Map<String, Phrase> normalize(final List<Phrase> ngrams) {
    final Map<String, Phrase> normalized = Maps.newLinkedHashMap();
    for (final Phrase ngram : ngrams) {
      // TODO: should be get normalized!
      normalized.put(ngram.getOriginal(), ngram);
    }
    return normalized;
  }

  public static List<Phrase> getUniqueBiGramsForWitnessA(final Witness a, final Witness b) {
    throw new UnsupportedOperationException("NOT YET IMPLEMENTED!");
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
  public static List<Phrase> getUniqueBiGramsForWitnessB(final Witness a, final Witness b) {
    final List<Subsegment2> overlappingBiGrams = getOverlappingBiGrams(a, b);
    throw new UnsupportedOperationException("NOT YET IMPLEMENTED!");

    // hmm hier heb ik weer de bigrams nodig van Witness B; dat wordt dan wel dubbel berekend.. zucht
    //    final List<Phrase> biGramsForWitnessB = calculate(b);
    //    final List<Phrase> uniqueBiGramsForWitnessB = Lists.newArrayList();
    //    uniqueBiGramsForWitnessB.addAll(biGramsForWitnessB);
    //
    //    for (final Subsegment2 overlappingBiGram : overlappingBiGrams) {
    //      final Phrase overlappingPhraseInWitnessB = overlappingBiGram.getPhraseFor(b.getFirstSegment().getWitnessId());
    //      // TODO: this is here because Phrase has no equals!
    //      // TODO: should be toTest.getNormalized!;
    //      // TODO: this is inefficient!
    //      for (final Phrase toTest : biGramsForWitnessB) {
    //        if (toTest.getOriginal().equals(overlappingPhraseInWitnessB.getOriginal())) {
    //          uniqueBiGramsForWitnessB.remove(toTest);
    //        }
    //      }
    //    }
    //    return uniqueBiGramsForWitnessB;
  }

  // TODO: this method is not finished!
  // TODO: does not work right for multiple groups of bigrams
  public static List<Phrase> getLongestUniquePiecesForWitnessA(final Witness a, final Witness b) {
    final List<Phrase> uniqueBiGramsForWitnessA = getUniqueBiGramsForWitnessA(a, b);
    final List<Phrase> newBiGrams = Lists.newArrayList();
    final Phrase currentBiGram = uniqueBiGramsForWitnessA.remove(0); // TODO: this can be dangerous; if there are no unique bigrams!
    for (final Phrase nextBiGram : uniqueBiGramsForWitnessA) {
      System.out.println(currentBiGram.getBeginPosition() + ":" + nextBiGram.getBeginPosition());
      final Phrase newBigram = new Phrase(currentBiGram.getWitness(), currentBiGram.getFirstWord(), nextBiGram.getLastWord(), null);
      newBiGrams.add(newBigram);
    }
    return newBiGrams;
  }

  // TODO: this method is not finished!
  public static List<Phrase> getLongestUniquePiecesForWitnessB(final Witness a, final Witness b) {
    final List<Phrase> uniqueBiGramsForWitnessB = getUniqueBiGramsForWitnessB(a, b);
    return uniqueBiGramsForWitnessB;
  }

}
