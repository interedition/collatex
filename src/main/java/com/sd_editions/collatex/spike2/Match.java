package com.sd_editions.collatex.spike2;

public class Match {
  private final Word word1;
  private final Word word2;
  public final Integer wordCode;

  //  public final Integer levenshteinDistance;

  @SuppressWarnings("boxing")
  public Match(Word word12, Word word22, Integer _wordCode) {
    word1 = word12;
    word2 = word22;
    this.wordCode = _wordCode;
  }

  @Override
  public String toString() {
    return "(" + word1.position + "->" + word2.position + ")";
  }

  public Word getWitnessWord() {
    return word2;
  }

  public Word getBaseWord() {
    return word1;
  }

  public boolean equals(Match other) {
    return this.word1.equals(other.word1) && this.word2.equals(other.word2) && this.wordCode.equals(other.wordCode);
  }
}
