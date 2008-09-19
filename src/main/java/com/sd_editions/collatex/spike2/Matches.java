package com.sd_editions.collatex.spike2;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class Matches {
  private final WitnessIndex base;
  private final WitnessIndex witness;

  public Matches(WitnessIndex _base, WitnessIndex _witness) {
    this.base = _base;
    this.witness = _witness;
  }

  // Integers are word codes
  public Set<Match> matches() {
    Set<Integer> matchesAsWordCodes = matchesAsWordCodes();
    Set<Match> matches = Sets.newLinkedHashSet();
    for (Integer matchAsWordCode : matchesAsWordCodes) {
      matches.add(convertWordCodeToMatch(base, witness, matchAsWordCode));
    }
    return matches;
  }

  private Set<Integer> matchesAsWordCodes() {
    Set<Integer> matchesAsWordCodes = Sets.newLinkedHashSet(base.getWordCodes());
    matchesAsWordCodes.retainAll(witness.getWordCodes());
    //    System.out.println(matchesAsWordCodes);
    return matchesAsWordCodes;
  }

  private static Match convertWordCodeToMatch(WitnessIndex base, WitnessIndex witness, Integer match) {
    Word word1 = base.getWordOnPosition(base.getPosition(match));
    Word word2 = witness.getWordOnPosition(witness.getPosition(match));
    return new Match(word1, word2, match);
  }

  public List<Integer> getSequenceOfMatchesInBase() {
    return Lists.newArrayList(matchesAsWordCodes());
  }

  public List<Integer> getSequenceOfMatchesInWitness() {
    return witness.sortMatchesByPosition(matchesAsWordCodes());
  }

}
