package eu.interedition.collatex.experimental.ngrams;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import eu.interedition.collatex.experimental.ngrams.data.Witness;

// TODO: note this is not really an index! this is a combination of one!
public class BiGramIndexGroup {

  private final BiGramIndex indexA;
  private final BiGramIndex indexB;

  public BiGramIndexGroup(final BiGramIndex indexA, final BiGramIndex indexB) {
    this.indexA = indexA;
    this.indexB = indexB;
  }

  public static BiGramIndexGroup create(final Witness a, final Witness b) {
    final BiGramIndex indexA = BiGramIndex.create(a);
    final BiGramIndex indexB = BiGramIndex.create(b);
    final BiGramIndexGroup group = new BiGramIndexGroup(indexA, indexB);
    return group;
  }

  public List<Subsegment2> getOverlap() {
    final Set<String> union = indexA.keys();
    union.retainAll(indexB.keys());
    //    System.out.println("union: " + union);
    final List<Subsegment2> subsegments = Lists.newArrayList();
    for (final String key : union) {
      final BiGram biGramA = indexA.get(key);
      final BiGram biGramB = indexB.get(key);
      final Subsegment2 subsegment = new Subsegment2(key, biGramA, biGramB);
      subsegments.add(subsegment);
    }
    return subsegments;
  }

  // TODO: this should return a BiGramIndex instead!
  public List<Subsegment2> getUniqueBiGramsForWitnessA() {
    final List<String> uniqueBigramsForWitnessANormalized = Lists.newArrayList(indexA.keys());
    uniqueBigramsForWitnessANormalized.removeAll(indexB.keys());
    // System.out.println(uniqueBigramsForWitnessANormalized);
    final List<Subsegment2> subsegments = Lists.newArrayList();
    for (final String key : uniqueBigramsForWitnessANormalized) {
      final BiGram phrase1 = indexA.get(key);
      final Subsegment2 subsegment = new Subsegment2(key, phrase1);
      subsegments.add(subsegment);
    }
    return subsegments;
  }

  // TODO: methods that are doing almost the same thing! That should not be necessary!
  //    // Until here is the exact same stuff as the other method!
  public List<BiGram> getUniqueBiGramsForWitnessB() {
    final List<String> result = Lists.newArrayList(indexB.keys());
    result.removeAll(indexA.keys());
    System.out.println(result);
    // The next part is also the same! (only the map were it comes from is different!
    final List<BiGram> subsegments = Lists.newArrayList();
    for (final String key : result) {
      final BiGram phrase1 = indexB.get(key);
      subsegments.add(phrase1);
    }
    return subsegments;
  }

  public List<NGram> getUniqueNGramsForWitnessB() {
    final List<BiGram> uniqueBiGramsForWitnessA = getUniqueBiGramsForWitnessB();
    final List<NGram> newNGrams = Lists.newArrayList();
    final NGram currentNGram = NGram.create(uniqueBiGramsForWitnessA.remove(0)); // TODO: this can be dangerous; if there are no unique bigrams!
    for (final BiGram nextBiGram : uniqueBiGramsForWitnessA) {
      //System.out.println(currentBiGram.getBeginPosition() + ":" + nextBiGram.getBeginPosition());
      currentNGram.add(nextBiGram);
      //   final Phrase newBigram = new Phrase(currentBiGram.getWitness(), currentBiGram.getFirstWord(), nextBiGram.getLastWord(), null);
      // newBiGrams.add(newBigram);
    }
    return Lists.newArrayList(currentNGram);
  }
}
