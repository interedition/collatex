package com.sd_editions.collatex.spike2;

public class Witness {
  private final Word[] words;

  public Witness(Word... words) {
    this.words = words;
  }

  public String getWordOnPosition(int k) {
    return (words[k - 1]).toString();
  }

}
