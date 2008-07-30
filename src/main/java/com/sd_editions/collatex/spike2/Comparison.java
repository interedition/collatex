package com.sd_editions.collatex.spike2;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sd_editions.collatex.spike2.collate.Addition;
import com.sd_editions.collatex.spike2.collate.Removal;
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
    for (Integer added_word : added_words) {
      modifications.add(new Addition(added_word, witnessIndex2.getPosition(added_word)));
    }
    for (Integer removed_word : removed_words) {
      modifications.add(new Removal(removed_word, witnessIndex.getPosition(removed_word)));
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
    int positionInFirstWitnessOfTransposedWord = distances.indexOf(maxTransposition);
    int transposedWord = witnessIndex.getWordOnPosition(positionInFirstWitnessOfTransposedWord);
    int positionInSecondWitnessOfTransposedWord = witnessIndex2.getPosition(transposedWord);
    List<Integer> witnessAsModified = Lists.newArrayList(witnessIndex.getWordCodes());
    witnessAsModified.remove(positionInFirstWitnessOfTransposedWord);
    witnessAsModified.add(positionInSecondWitnessOfTransposedWord, transposedWord);
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
    int sourcePosition = 0;
    for (Integer word : words) {
      int destPosition = witness2.getPosition(word);
      int distance = Math.abs(destPosition - sourcePosition);
      distances.add(distance);
      sourcePosition++;
    }
    return distances;
  }
}
