package com.sd_editions.collatex.match;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.google.common.collect.SortedArraySet;
import com.google.common.collect.TreeMultimap;

public class WordMatches {
  private final String word;
  private final SortedArraySet<WordCoordinate> exactMatches = Sets.newSortedArraySet();
  private final SortedArraySet<WordCoordinate> levMatches = Sets.newSortedArraySet();

  public WordMatches(String newWord) {
    this.word = newWord;
  }

  public String getWord() {
    return word;
  }

  public void addExactMatch(WordCoordinate matchCoordinate) {
    exactMatches.add(matchCoordinate);
  }

  public void addLevMatch(WordCoordinate matchCoordinate) {
    levMatches.add(matchCoordinate);
  }

  public SortedArraySet<WordCoordinate> getExactMatches() {
    return exactMatches;
  }

  public SortedArraySet<WordCoordinate> getLevMatches() {
    return levMatches;
  }

  public Set<WordCoordinate> getAllMatches() {
    Set<WordCoordinate> all = exactMatches;
    all.addAll(levMatches);
    return all;
  }

  @SuppressWarnings("boxing")
  public List<WordMatches> getPermutations() {
    TreeMultimap<Integer, Integer> matchesPerWitness = Multimaps.newTreeMultimap();
    for (WordCoordinate wordCoordinate : getAllMatches()) {
      matchesPerWitness.put(wordCoordinate.witnessNumber, wordCoordinate.positionInWitness);
    }

    List<TreeMultimap<Integer, Integer>> matchVectors = getPermutations(matchesPerWitness);

    List<WordMatches> list = Lists.newArrayList();
    for (TreeMultimap<Integer, Integer> vector : matchVectors) {
      WordMatches wordMatch = new WordMatches(this.word);
      for (Integer key : vector.keySet()) {
        wordMatch.addExactMatch(new WordCoordinate(key.intValue(), vector.get(key).iterator().next().intValue()));
      }
      list.add(wordMatch);
    }
    return list;
  }

  private List<TreeMultimap<Integer, Integer>> getPermutations(TreeMultimap<Integer, Integer> matchesPerWitness) {
    List<TreeMultimap<Integer, Integer>> list = Lists.newArrayList();
    Iterator<Integer> witnessIterator = matchesPerWitness.keySet().iterator();
    boolean goOn = witnessIterator.hasNext();
    while (goOn) {
      Integer witnessId = witnessIterator.next();
      final Set<Integer> positionInWitnessSet = matchesPerWitness.get(witnessId);
      if (positionInWitnessSet.size() > 1) {
        for (Integer position : positionInWitnessSet) {
          TreeMultimap<Integer, Integer> newMatchesPerWitness = Multimaps.newTreeMultimap();
          newMatchesPerWitness.putAll(matchesPerWitness);
          newMatchesPerWitness.removeAll(witnessId);
          newMatchesPerWitness.put(witnessId, position);
          list.addAll(getPermutations(newMatchesPerWitness));
        }
        goOn = false;
      } else {
        goOn = witnessIterator.hasNext();
      }
    }
    if (list.isEmpty()) list.add(matchesPerWitness);
    return list;
  }

  @Override
  public String toString() {
    return word + ": exact=" + exactMatches + ", lev=" + levMatches;
  }

}
