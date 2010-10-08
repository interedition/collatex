package eu.interedition.collatex.alignment;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.input.BaseElement;

public class MatchSequence<T extends BaseElement> {
  private final List<Match<T>> sequence;
  public final Integer code;

  public MatchSequence(final Integer _code, final Match<T>... matches) {
    sequence = Lists.newArrayList(matches);
    code = _code;
  }

  @Override
  public String toString() {
    return sequence.toString();
  }

  public void add(final Match<T> match) {
    sequence.add(match);
  }

  public boolean isEmpty() {
    return sequence.isEmpty();
  }

  public Match<T> getFirstMatch() {
    return sequence.get(0);
  }

  // TODO rename to getWitnessBeginPosition!
  @SuppressWarnings("boxing")
  public Integer getSegmentPosition() {
    return getFirstWitnessWord().getBeginPosition();
  }

  // TODO rename to getBaseWitnessBeginPosition!
  @SuppressWarnings("boxing")
  public Integer getBasePosition() {
    return getFirstBaseWord().getBeginPosition();
  }

  //TODO rename Word to Element!
  private T getFirstWitnessWord() {
    return getFirstMatch().getWitnessWord();
  }

  //TODO rename Word to Element!
  private T getFirstBaseWord() {
    return getFirstMatch().getBaseWord();
  }

  public String baseToString() {
    StringBuilder result = new StringBuilder();
    String delimiter = "";
    for (int i = 0; i < sequence.size(); i++) {
      final T baseWord = sequence.get(i).getBaseWord();
      if (i > 0 && (baseWord.getBeginPosition() - sequence.get(i - 1).getBaseWord().getBeginPosition()) > 1) {
        result.append(delimiter).append("...");
      }
      result.append(delimiter).append(baseWord.toString());
      delimiter = " ";
    }
    return result.toString();
  }

  public List<Match<T>> getMatches() {
    return sequence;
  }

  public Match<T> getLastMatch() {
    return sequence.get(sequence.size() - 1);
  }
}
