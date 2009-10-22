package eu.interedition.collatex.alignment;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.input.Word;

public class MatchSequence {
  private final List<Match> sequence;
  public final Integer code;

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
  public Integer getSegmentPosition() {
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
    for (int i = 0; i < sequence.size(); i++) {
      Word baseWord = sequence.get(i).getBaseWord();
      if (i > 0 && (baseWord.position - sequence.get(i - 1).getBaseWord().position) > 1) {
        result += delimiter + "...";
      }
      result += delimiter + baseWord.toString();
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
