package com.sd_editions.collatex.spike2;

import java.util.List;

import com.google.common.collect.Lists;

public class MatchSequence {
  private final List<Match> sequence;
  final Integer code;

  public MatchSequence(Integer _code, Match... matches) {
    sequence = Lists.newArrayList(matches);
    code = _code;
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

  public Match getFirstMatch() {
    return sequence.get(0);
  }

  @SuppressWarnings("boxing")
  public Integer getWitnessPosition() {
    return getFirstWitnessWord().position;
  }

  @SuppressWarnings("boxing")
  public Integer getBasePosition() {
    return getFirstBaseWord().position;
  }

  private Word getFirstWitnessWord() {
    return getFirstMatch().getWitnessWord();
  }

  private Word getFirstBaseWord() {
    return getFirstMatch().getBaseWord();
  }

  public String baseToString() {
    String result = "";
    String delimiter = "";
    for (Match match : sequence) {
      result += delimiter + match.getBaseWord().toString();
      delimiter = " ";
    }
    return result;
  }

  public List<Match> getMatches() {
    return sequence;
  }

  public Match getLastMatch() {
    return sequence.get(sequence.size() - 1);
  }
}
