package eu.interedition.collatex.alignment;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.input.BaseElement;

public class MatchSequence<T extends BaseElement> {
  private final List<Match<T>> sequence;
  public final Integer code;

  public MatchSequence(Integer _code, Match<T>... matches) {
    sequence = Lists.newArrayList(matches);
    code = _code;
  }

  @Override
  public String toString() {
    return sequence.toString();
  }

  public void add(Match<T> match) {
    sequence.add(match);
  }

  public boolean isEmpty() {
    return sequence.isEmpty();
  }

  public Match<T> getFirstMatch() {
    return sequence.get(0);
  }

  @SuppressWarnings("boxing")
  public Integer getSegmentPosition() {
    return getFirstWitnessWord().getPosition();
  }

  @SuppressWarnings("boxing")
  public Integer getBasePosition() {
    return getFirstBaseWord().getPosition();
  }

  private T getFirstWitnessWord() {
    return getFirstMatch().getWitnessWord();
  }

  private T getFirstBaseWord() {
    return getFirstMatch().getBaseWord();
  }

  public String baseToString() {
    String result = "";
    String delimiter = "";
    for (int i = 0; i < sequence.size(); i++) {
      T baseWord = sequence.get(i).getBaseWord();
      if (i > 0 && (baseWord.getPosition() - sequence.get(i - 1).getBaseWord().getPosition()) > 1) {
        result += delimiter + "...";
      }
      result += delimiter + baseWord.toString();
      delimiter = " ";
    }
    return result;
  }

  public List<Match<T>> getMatches() {
    return sequence;
  }

  public Match<T> getLastMatch() {
    return sequence.get(sequence.size() - 1);
  }
}
