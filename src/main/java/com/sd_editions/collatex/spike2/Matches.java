package com.sd_editions.collatex.spike2;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class Matches {

  private final Set<Integer> matches;
  private final WitnessIndex witnessIndex;
  private final WitnessIndex witnessIndex2;

  public Matches(WitnessIndex _witnessIndex, WitnessIndex _witnessIndex2) {
    this.witnessIndex = _witnessIndex;
    this.witnessIndex2 = _witnessIndex2;
    matches = Sets.newLinkedHashSet(witnessIndex.getWordCodes());
    matches.retainAll(witnessIndex2.getWordCodes());
    //    System.out.println(matches);
  }

  public List<Gap> getGaps() {
    List<Gap> gaps = Lists.newArrayList();
    List<PositionTuple> tuples = getMatchesSortedByPosition();
    int currentBaseIndex = 1;
    int currentWitnessIndex = 1;
    for (PositionTuple tuple : tuples) {
      int baseIndexDif = tuple.baseIndex - currentBaseIndex;
      int witnessIndexDif = tuple.witnessIndex - currentWitnessIndex;
      Gap gap = new Gap(baseIndexDif, witnessIndexDif, witnessIndex, witnessIndex2);
      gaps.add(gap);
      gap.baseBeginPosition = currentBaseIndex;
      gap.baseEndPosition = tuple.baseIndex - 1;
      gap.witnessBeginPosition = currentWitnessIndex;
      gap.witnessEndPosition = tuple.witnessIndex - 1;
      currentBaseIndex = 1 + tuple.baseIndex;
      currentWitnessIndex = 1 + tuple.witnessIndex;
    }
    int baseIndexDif = witnessIndex.size() - currentBaseIndex + 1;
    int witnessIndexDif = witnessIndex2.size() - currentWitnessIndex + 1;
    Gap gapAtTheEnd = new Gap(baseIndexDif, witnessIndexDif, witnessIndex, witnessIndex2);
    gaps.add(gapAtTheEnd);
    gapAtTheEnd.baseBeginPosition = currentBaseIndex;
    gapAtTheEnd.baseEndPosition = witnessIndex.size();
    gapAtTheEnd.witnessBeginPosition = currentWitnessIndex;
    gapAtTheEnd.witnessEndPosition = witnessIndex2.size();
    return Lists.newArrayList(Iterables.filter(gaps, new NonEmptyGapPredicate())); // TODO: this can be done easier!
  }

  @SuppressWarnings("boxing")
  private List<PositionTuple> getMatchesSortedByPosition() {
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
    return tuples;
  }

  public class PositionTuple {
    public final int baseIndex;
    public final int witnessIndex;

    public PositionTuple(int _positionWitness1, int _positionWitness2) {
      this.baseIndex = _positionWitness1;
      this.witnessIndex = _positionWitness2;
    }

  }
}
