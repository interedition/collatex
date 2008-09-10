package com.sd_editions.collatex.spike2;

public class Match {
  private final Word word1;
  private final Word word2;

  public Match(Word word12, Word word22) {
    word1 = word12;
    word2 = word22;
  }

  @Override
  public String toString() {
    return "(" + word1.position + "->" + word2.position + ")";
  }
}
