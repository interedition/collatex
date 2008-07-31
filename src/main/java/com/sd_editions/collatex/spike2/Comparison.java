package com.sd_editions.collatex.spike2;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sd_editions.collatex.spike2.collate.Addition;
import com.sd_editions.collatex.spike2.collate.Removal;
import com.sd_editions.collatex.spike2.collate.Replacement;
import com.sd_editions.collatex.spike2.collate.Transposition;

public class Comparison {

  private final List<Modification> modifications;
  private final WitnessIndex witnessIndex;
  private final WitnessIndex witnessIndex2;

  @SuppressWarnings("boxing")
  public Comparison(WitnessIndex _witnessIndex, WitnessIndex _witnessIndex2) {
    this.witnessIndex = _witnessIndex;
    this.witnessIndex2 = _witnessIndex2;
    modifications = Lists.newArrayList();
    // hier gaan we dus weer die tuple array van vroeger bepalen
    // bepaal de matches..
    // dat is een set van integers...
    // bepaal daarna de posities in de beide witnesses waar matches voorkomen
    // sorteer voor beide posities van links naar rechts..
    // voila daar is je tuple array
    Set<Integer> matches = Sets.newLinkedHashSet(witnessIndex.getWordCodes());
    matches.retainAll(witnessIndex2.getWordCodes());
    System.out.println(matches);
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
    int currentBaseIndex = 0;
    int currentWitnessIndex = 0;
    for (PositionTuple tuple : tuples) {
      System.out.println("baseIndex: " + currentBaseIndex + "; witnessIndex: " + currentWitnessIndex);
      int baseIndexDif = tuple.baseIndex - currentBaseIndex;
      int witnessIndexDif = tuple.witnessIndex - currentWitnessIndex;
      System.out.println("differences: " + baseIndexDif + "; " + witnessIndexDif);
      if (baseIndexDif > 1 && witnessIndexDif > 1) {
        //        List<Word> replacementWords = witness.getPhrase(currentWitnessIndex + 1, tuple.witnessIndex - 1);
        //        table.setReplacement(witnessNumber, currentBaseIndex + 1, replacementWords);
        // TODO: currentBaseIndex should also be a range!
        modifications.add(new Replacement(witnessIndex, currentBaseIndex + 1, witnessIndex2, currentWitnessIndex + 1, tuple.witnessIndex - 1));
      } else if (baseIndexDif > 1 && witnessIndexDif == 1) {
        modifications.add(new Removal(witnessIndex, currentBaseIndex + 1, tuple.baseIndex));
        //        table.setOmission(witnessNumber, currentBaseIndex + 1, tuple.baseIndex);
      } else if (baseIndexDif == 1 && witnessIndexDif > 1) {
        Phrase addition = witnessIndex2.createPhrase(currentWitnessIndex + 1, tuple.witnessIndex - 1);
        modifications.add(new Addition(currentBaseIndex + 1, addition));
      }
      currentBaseIndex = tuple.baseIndex;
      currentWitnessIndex = tuple.witnessIndex;
      //      table.setIdenticalOrVariant(witnessNumber, currentBaseIndex, witness.get(currentWitnessIndex), tuple);
    }

    int baseIndexDif = witnessIndex.size() - currentBaseIndex;
    int witnessIndexDif = witnessIndex2.size() - currentWitnessIndex;
    if (baseIndexDif > 0 && witnessIndexDif > 0) {
      //      table.setReplacement(witnessNumber, currentBaseIndex + 1, witness.getPhrase(currentWitnessIndex + 1, witness.size()));
      modifications.add(new Replacement(witnessIndex, currentBaseIndex + 1, witnessIndex2, currentWitnessIndex + 1, witnessIndex2.size()));
    } else if (baseIndexDif > 0 && witnessIndexDif == 0) {
      //      table.setOmission(witnessNumber, currentBaseIndex + 1, base.size() + 1);
      modifications.add(new Removal(witnessIndex, currentBaseIndex + 1, witnessIndex.size() + 1));
    } else if (baseIndexDif == 0 && witnessIndexDif > 0) {
      Phrase addition = witnessIndex2.createPhrase(currentWitnessIndex + 1, witnessIndex2.size());
      modifications.add(new Addition(currentBaseIndex + 1, addition));
    }
  }

  public List<Modification> getModifications() {
    return modifications;
  }

  @SuppressWarnings("boxing")
  public List<Transposition> getTranspositions() {
    List<Integer> distances = calculateDistancesBetweenWitnesses(witnessIndex, witnessIndex2);
    int maxTransposition = Collections.max(distances);
    int positionInFirstWitnessOfTransposedWord = 1 + distances.indexOf(maxTransposition);
    int transposedWord = witnessIndex.getWordCodeOnPosition(positionInFirstWitnessOfTransposedWord);
    int positionInSecondWitnessOfTransposedWord = 1 + witnessIndex2.getPosition(transposedWord);
    List<Integer> witnessAsModified = Lists.newArrayList(witnessIndex.getWordCodes());
    witnessAsModified.remove(positionInFirstWitnessOfTransposedWord - 1);
    witnessAsModified.add(positionInSecondWitnessOfTransposedWord - 1, transposedWord);
    List<Transposition> transpositions = Lists.newArrayList();
    transpositions.add(new Transposition(transposedWord, maxTransposition));
    // TODO: make it a while loop!
    //        List<Integer> newDistances = calculateDistanceWithList(witnessIndex2, witnessAsModified);
    //    maxTransposition = Collections.max(newDistances);

    return transpositions;
  }

  private List<Integer> calculateDistancesBetweenWitnesses(WitnessIndex witness1, WitnessIndex witness2) {
    List<Integer> words = witness1.getWordCodesList();
    return calculateDistanceWithList(witness2, words);
  }

  @SuppressWarnings("boxing")
  private List<Integer> calculateDistanceWithList(WitnessIndex witness2, List<Integer> words) {
    List<Integer> distances = Lists.newArrayList();
    int sourcePosition = 1;
    for (Integer word : words) {
      int destPosition = witness2.getPosition(word);
      int distance = Math.abs(destPosition - sourcePosition);
      distances.add(distance);
      sourcePosition++;
    }
    return distances;
  }
}
