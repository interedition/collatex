/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.sd_editions.collatex.match;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;

public class WordMatches {
  private final String word;
  private final SortedSet<WordCoordinate> exactMatches = Sets.newTreeSet();
  private final SortedSet<WordCoordinate> levMatches = Sets.newTreeSet();

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

  public SortedSet<WordCoordinate> getExactMatches() {
    return exactMatches;
  }

  public SortedSet<WordCoordinate> getLevMatches() {
    return levMatches;
  }

  public Set<WordCoordinate> getAllMatches() {
    Set<WordCoordinate> all = exactMatches;
    all.addAll(levMatches);
    return all;
  }

  @SuppressWarnings("boxing")
  public List<WordMatches> getPermutations() {
    TreeMultimap<Integer, Integer> matchesPerWitness = TreeMultimap.create();
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
          TreeMultimap<Integer, Integer> newMatchesPerWitness = TreeMultimap.create();
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
