package com.sd_editions.collatex.spike2;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.base.Predicate;
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
  private Set<Integer> matches() {
    Set<Integer> matches = Sets.newLinkedHashSet(base.getWordCodes());
    matches.retainAll(witness.getWordCodes());
    //    System.out.println(matches);
    return matches;
  }

  // step 1 take the matches
  // step 2 walk over the witness index and filter away everything that is not a match

  protected static List<Integer> sortMatchesByPosition(final Set<Integer> matches, WitnessIndex witness) {
    List<Integer> wordCodesList = witness.getWordCodesList();
    List<Integer> onlyMatches = Lists.newArrayList(Iterables.filter(wordCodesList, new Predicate<Integer>() {
      public boolean apply(Integer wordCode) {
        return matches.contains(wordCode);
      }
    }));
    return onlyMatches;
  }

  public List<Integer> getSequenceOfMatchesInBase() {
    return Lists.newArrayList(matches());
  }

  public List<Integer> getSequenceOfMatchesInWitness() {
    return sortMatchesByPosition(matches(), witness);
  }

  public List<Gap> getGapsForBase() {
    return getGapsForWitness(base);
  }

  public List<Gap> getGapsForWitness() {
    return getGapsForWitness(witness);
  }

  @SuppressWarnings("boxing")
  private List<Gap> getGapsForWitness(WitnessIndex witnessIndex) {
    int currentIndex = 1;
    List<Integer> positions = getPositionsOfMatchesInSequence(witnessIndex);
    List<Gap> gaps = Lists.newArrayList();
    for (Integer position : positions) {
      int indexDif = position - currentIndex;
      gaps.add(new Gap(witnessIndex, indexDif, currentIndex, position - 1));
      currentIndex = 1 + position;
    }
    int IndexDif = witnessIndex.size() - currentIndex + 1;
    gaps.add(new Gap(witnessIndex, IndexDif, currentIndex, witnessIndex.size()));
    System.out.println(gaps);
    return gaps;
  }

  @SuppressWarnings("boxing")
  private List<Integer> getPositionsOfMatchesInSequence(WitnessIndex witnessIndex) {
    List<Integer> matchPositions = Lists.newArrayList();
    for (Integer match : matches()) {
      matchPositions.add(witnessIndex.getPosition(match));
    }
    Collections.sort(matchPositions);
    return matchPositions;
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
