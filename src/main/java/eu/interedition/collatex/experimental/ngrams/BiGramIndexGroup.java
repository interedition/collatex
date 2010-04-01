package eu.interedition.collatex.experimental.ngrams;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import eu.interedition.collatex.interfaces.IWitness;

// TODO note this is not really an index! this is a combination of two!
// TODO this class is gonna be very similar to WitnessSet!
public class BiGramIndexGroup {

  private final BiGramIndex indexA;
  private final BiGramIndex indexB;

  public BiGramIndexGroup(final BiGramIndex indexA, final BiGramIndex indexB) {
    this.indexA = indexA;
    this.indexB = indexB;
  }

  public static BiGramIndexGroup create(final IWitness a, final IWitness b) {
    final BiGramIndex indexA = BiGramIndex.create(a);
    final BiGramIndex indexB = BiGramIndex.create(b);
    final BiGramIndexGroup group = new BiGramIndexGroup(indexA, indexB);
    return group;
  }

  // TODO make it return a NGramIndex!
  public List<NGram> getUniqueNGramsForWitnessA() {
    final BiGramIndex bigrams = getUniqueBigramsForWitnessA();
    return NGramIndex.concatenateBiGramToNGram(bigrams);
  }

  // TODO make it return a NGramIndex!
  public List<NGram> getUniqueNGramsForWitnessB() {
    final BiGramIndex biGramIndex = new BiGramIndex(getUniqueBiGramsForWitnessB());
    return NGramIndex.concatenateBiGramToNGram(biGramIndex);
  }

  // TODO This method is only public for test reasons!
  // TODO move test to another package and make this method protected instead!
  public BiGramIndex getUniqueBigramsForWitnessA() {
    final List<String> uniqueBigramsForWitnessANormalized = Lists.newArrayList(indexA.keys());
    uniqueBigramsForWitnessANormalized.removeAll(indexB.keys());
    System.out.println(uniqueBigramsForWitnessANormalized);
    final List<BiGram> bigrams = Lists.newArrayList();
    for (final String key : uniqueBigramsForWitnessANormalized) {
      final BiGram bigram = indexA.get(key);
      bigrams.add(bigram);
    }
    final BiGramIndex index = new BiGramIndex(bigrams);
    return index;
  }

  // TODO methods that are doing almost the same thing! That should not be necessary!
  //    // Until here is the exact same stuff as the other method!
  // TODO make private!
  // TODO make return type a BiGramIndex!
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

  // TODO no longer used? remove!
  public List<BiGram> getOverlappingBiGramsForWitnessA() {
    final Set<String> union = indexA.keys();
    union.retainAll(indexB.keys());
    //    System.out.println("union: " + union);
    final List<BiGram> bigrams = Lists.newArrayList();
    for (final String key : union) {
      final BiGram biGramA = indexA.get(key);
      bigrams.add(biGramA);
    }
    return bigrams;
  }

  // TODO no longer used? remove!
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

}
