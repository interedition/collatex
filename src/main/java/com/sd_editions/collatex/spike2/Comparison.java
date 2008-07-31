package com.sd_editions.collatex.spike2;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sd_editions.collatex.spike2.collate.Addition;
import com.sd_editions.collatex.spike2.collate.Removal;
import com.sd_editions.collatex.spike2.collate.Replacement;
import com.sd_editions.collatex.spike2.collate.Transposition;

public class Comparison {

  private final Index colors;
  private final Set<Integer> added_words;
  private final Set<Integer> removed_words;
  private final List<Modification> modifications;
  private final WitnessIndex witnessIndex;
  private final WitnessIndex witnessIndex2;

  @SuppressWarnings("boxing")
  public Comparison(WitnessIndex _witnessIndex, WitnessIndex _witnessIndex2) {
    this.witnessIndex = _witnessIndex;
    this.witnessIndex2 = _witnessIndex2;
    added_words = Sets.newLinkedHashSet(witnessIndex2.getWordCodes());
    added_words.removeAll(witnessIndex.getWordCodes());
    removed_words = Sets.newLinkedHashSet(witnessIndex.getWordCodes());
    removed_words.removeAll(witnessIndex2.getWordCodes());
    this.colors = witnessIndex.getIndex();
    modifications = Lists.newArrayList();
    //    for (Integer added_word : added_words) {
    //      modifications.add(new Addition(added_word, witnessIndex2));
    //    }
    //    for (Integer removed_word : removed_words) {
    //      modifications.add(new Removal(removed_word, witnessIndex.getPosition(removed_word)));
    //    }

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
    // sorting can be done later!
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
        //        List<Word> additionalWords = witness.getPhrase(currentWitnessIndex + 1, tuple.witnessIndex - 1);
        //        table.setFrontAddition(witnessNumber, currentBaseIndex + 1, additionalWords);
        modifications.add(new Addition(currentWitnessIndex + 1, witnessIndex2)); // TODO: should become a phrase!
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
      //      List<Word> additionalWords = witness.getPhrase(currentWitnessIndex + 1, witness.size());
      //      table.setBackAddition(witnessNumber, currentBaseIndex, additionalWords);
      modifications.add(new Addition(currentWitnessIndex + 1, witnessIndex2)); // TODO: should become a phrase!
    }
  }

  //TODO: remove!
  public List<String> getAddedWords() {
    List<String> additions = Lists.newArrayList();
    for (Integer color : added_words) {
      additions.add(getWordForColor(color));
    }
    return additions;
  }

  //TODO: remove!
  @SuppressWarnings("boxing")
  private String getWordForColor(Integer color) {
    return colors.getWord(color);
  }

  public List<Modification> getModifications() {
    return modifications;
  }

  public List<String> getReplacedWords() {
    // NOTE: this far too simple!
    List<String> replacements = Lists.newArrayList();
    if (added_words.size() == removed_words.size()) {
      Iterator<Integer> rem = removed_words.iterator();
      for (Integer add : added_words) {
        Integer remove = rem.next();
        replacements.add(getWordForColor(remove) + "/" + getWordForColor(add));
      }
    }
    return replacements;
  }

  //TODO: remove!
  public List<String> getRemovedWords() {
    List<String> removals = Lists.newArrayList();
    for (Integer color : removed_words) {
      removals.add(getWordForColor(color));
    }
    return removals;
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
