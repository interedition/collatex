package com.sd_editions.collatex.spike2;

import java.util.List;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class NewMatches {
  private final WitnessIndex base;
  private final WitnessIndex witness;

  public NewMatches(WitnessIndex base, WitnessIndex witness) {
    this.base = base;
    this.witness = witness;
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

  //  public List<Gap> getGapsForBase() {
  //    Set
  //  }

}
