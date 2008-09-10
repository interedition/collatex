package com.sd_editions.collatex.spike2;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Iterables;
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
    Word word1 = base.getNewWordOnPosition(base.getPosition(match));
    Word word2 = witness.getNewWordOnPosition(witness.getPosition(match));
    return new Match(word1, word2, match);
  }

  public List<Integer> getSequenceOfMatchesInBase() {
    return Lists.newArrayList(matchesAsWordCodes());
  }

  public List<Integer> getSequenceOfMatchesInWitness() {
    return witness.sortMatchesByPosition(matchesAsWordCodes());
  }

  public List<Gap> getGapsForBase() {
    return base.getGaps(matchesAsWordCodes());
  }

  public List<Gap> getGapsForWitness() {
    return witness.getGaps(matchesAsWordCodes());
  }

  public List<MisMatch> getMisMatches() {
    List<Gap> gapsForBase = getGapsForBase();
    List<Gap> gapsForWitness = getGapsForWitness();
    List<MisMatch> mismatches = Lists.newArrayList();
    for (int i = 0; i < gapsForBase.size(); i++) {
      Gap _base = gapsForBase.get(i);
      Gap _witness = gapsForWitness.get(i);
      mismatches.add(new MisMatch(_base, _witness));
    }
    return Lists.newArrayList(Iterables.filter(mismatches, new ValidMismatchPredicate())); // TODO: this can be done easier!
  }
}
