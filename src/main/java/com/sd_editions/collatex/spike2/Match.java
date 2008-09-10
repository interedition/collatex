package com.sd_editions.collatex.spike2;

public class Match {
  private final Word word1;
  private final Word word2;
  public final Integer wordCode;

  public Match(Word word12, Word word22, Integer wordCode) {
    word1 = word12;
    word2 = word22;
    this.wordCode = wordCode;
  }

  @Override
  public String toString() {
    return "(" + word1.position + "->" + word2.position + ")";
  }
}
