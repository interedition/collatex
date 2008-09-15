package com.sd_editions.collatex.spike2;

public class Word {
  private final String original;
  final int position;

  public Word(String original, int position) {
    this.original = original;
    this.position = position;
  }

  @Override
  public String toString() {
    return original;
  }
}
