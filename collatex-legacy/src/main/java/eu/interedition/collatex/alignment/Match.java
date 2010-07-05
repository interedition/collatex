package eu.interedition.collatex.alignment;

import eu.interedition.collatex.input.BaseElement;

public class Match<T extends BaseElement> implements Comparable<Match> {
  private final T word1;
  private final T word2;
  public final float wordDistance;

  public Match(final T baseWord, final T witnessWord) {
    this(baseWord, witnessWord, 0);
  }

  public Match(final T baseWord, final T witnessWord, final float levDistance) {
    this.word1 = baseWord;
    this.word2 = witnessWord;
    this.wordDistance = levDistance;
  }

  @Override
  public String toString() {
    return "(" + word1.getBeginPosition() + "->" + word2.getBeginPosition() + ")";
  }

  public T getWitnessWord() {
    return word2;
  }

  public T getBaseWord() {
    return word1;
  }

  @Override
  public boolean equals(final Object _other) {
    if (!(_other instanceof Match)) {
      return false;
    }
    final Match other = (Match) _other;
    return this.word1.equals(other.word1) && this.word2.equals(other.word2);
  }

  @Override
  public int hashCode() {
    return word1.hashCode() + word2.hashCode();
  }

  public int compareTo(final Match m2) {
    return getBaseWord().getBeginPosition() - m2.getBaseWord().getBeginPosition();
  }
}
