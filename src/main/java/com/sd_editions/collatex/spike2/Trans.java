package com.sd_editions.collatex.spike2;

import java.util.List;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class Trans {

  private final Set<TranspositionTuple> transpositions;

  public Trans(List<MatchSequence> sequences) {
    this.transpositions = calculateTranspositions(sequences);
  }

  private Set<TranspositionTuple> calculateTranspositions(List<MatchSequence> sequences) {
    List<MatchSequence> filteredMatchSequence = Lists.newArrayList(Iterables.filter(sequences, new Predicate<MatchSequence>() {
      public boolean apply(MatchSequence sequence) {
        return sequence.getBasePosition() != sequence.getWitnessPosition();
      }
    }));
    List<TranspositionTuple> asTranspositionTuples = Lists.newArrayList();
    for (MatchSequence sequence : filteredMatchSequence) {
      asTranspositionTuples.add(new TranspositionTuple(sequence));
    }
    return Sets.newHashSet(asTranspositionTuples);
  }

  public Set<TranspositionTuple> getTranspositions() {
    return transpositions;
  }

}
