package com.sd_editions.collatex.spike2;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sd_editions.collatex.spike2.collate.Addition;
import com.sd_editions.collatex.spike2.collate.Removal;
import com.sd_editions.collatex.spike2.collate.Replacement;

public class Comparison {

  private final List<Modification> modifications;
  private final WitnessIndex witnessIndex;
  private final WitnessIndex witnessIndex2;

  public Comparison(WitnessIndex _witnessIndex, WitnessIndex _witnessIndex2) {
    this.witnessIndex = _witnessIndex;
    this.witnessIndex2 = _witnessIndex2;
    modifications = Lists.newArrayList();
    List<PositionTuple> tuples = calculateTuples();
    calculateModifications(tuples);
  }

  @SuppressWarnings("boxing")
  private List<PositionTuple> calculateTuples() {
    Set<Integer> matches = Sets.newLinkedHashSet(witnessIndex.getWordCodes());
    matches.retainAll(witnessIndex2.getWordCodes());
    //    System.out.println(matches);
    List<Integer> matchPositionsInWitness1 = Lists.newArrayList();
    List<Integer> matchPositionsInWitness2 = Lists.newArrayList();
    for (Integer match : matches) {
      matchPositionsInWitness1.add(witnessIndex.getPosition(match));
      matchPositionsInWitness2.add(witnessIndex2.getPosition(match));
    }
    Collections.sort(matchPositionsInWitness1);
    Collections.sort(matchPositionsInWitness2);
    List<PositionTuple> tuples = Lists.newArrayList();
    int i = 0;
    for (Integer position : matchPositionsInWitness1) {
      Integer position2 = matchPositionsInWitness2.get(i);
      tuples.add(new PositionTuple(position, position2));
      i++;
    }
    return tuples;
  }

  private void calculateModifications(List<PositionTuple> tuples) {
    int currentBaseIndex = 0;
    int currentWitnessIndex = 0;
    for (PositionTuple tuple : tuples) {
      //      System.out.println("baseIndex: " + currentBaseIndex + "; witnessIndex: " + currentWitnessIndex);
      int baseIndexDif = tuple.baseIndex - currentBaseIndex;
      int witnessIndexDif = tuple.witnessIndex - currentWitnessIndex;
      //      System.out.println("differences: " + baseIndexDif + "; " + witnessIndexDif);
      if (baseIndexDif > 1 && witnessIndexDif > 1) {
        Phrase original = witnessIndex.createPhrase(currentBaseIndex + 1, tuple.baseIndex - 1);
        Phrase replacement = witnessIndex2.createPhrase(currentWitnessIndex + 1, tuple.witnessIndex - 1);
        modifications.add(new Replacement(original, replacement));
      } else if (baseIndexDif > 1 && witnessIndexDif == 1) {
        modifications.add(new Removal(witnessIndex.createPhrase(currentBaseIndex + 1, tuple.baseIndex - 1)));
      } else if (baseIndexDif == 1 && witnessIndexDif > 1) {
        Phrase addition = witnessIndex2.createPhrase(currentWitnessIndex + 1, tuple.witnessIndex - 1);
        modifications.add(new Addition(currentBaseIndex + 1, addition));
      }
      currentBaseIndex = tuple.baseIndex;
      currentWitnessIndex = tuple.witnessIndex;
    }

    int baseIndexDif = witnessIndex.size() - currentBaseIndex;
    int witnessIndexDif = witnessIndex2.size() - currentWitnessIndex;
    if (baseIndexDif > 0 && witnessIndexDif > 0) {
      Phrase original = witnessIndex.createPhrase(currentWitnessIndex + 1, witnessIndex.size());
      Phrase replacement = witnessIndex2.createPhrase(currentWitnessIndex + 1, witnessIndex2.size());
      modifications.add(new Replacement(original, replacement));
    } else if (baseIndexDif > 0 && witnessIndexDif == 0) {
      modifications.add(new Removal(witnessIndex.createPhrase(currentBaseIndex + 1, witnessIndex.size())));
    } else if (baseIndexDif == 0 && witnessIndexDif > 0) {
      Phrase addition = witnessIndex2.createPhrase(currentWitnessIndex + 1, witnessIndex2.size());
      modifications.add(new Addition(currentBaseIndex + 1, addition));
    }
  }

  public List<Modification> getModifications() {
    return modifications;
  }

}
