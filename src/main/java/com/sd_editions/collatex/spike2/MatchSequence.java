package com.sd_editions.collatex.spike2;

import java.util.List;

import com.google.common.collect.Lists;

public class MatchSequence {
  private final List<Match> sequence;

  public MatchSequence() {
    sequence = Lists.newArrayList();
  }

  @Override
  public String toString() {
    return sequence.toString();
  }

  public void add(Match match) {
    sequence.add(match);
  }

  public boolean isEmpty() {
    return sequence.isEmpty();
  }

  @SuppressWarnings("boxing")
  public Integer getWitnessPosition() {
    return sequence.get(0).getWitnessWord().position;
  }

  public Integer getBasePosition() {
    return sequence.get(0).getBaseWord().position;
  }

}
