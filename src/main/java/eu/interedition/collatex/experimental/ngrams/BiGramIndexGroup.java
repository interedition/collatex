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
}
