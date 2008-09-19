package com.sd_editions.collatex.spike2;

public class Word {
  private final String original;
  final int position;

  public Word(String _original, int _position) {
    this.original = _original;
    this.position = _position;
  }

  @Override
  public String toString() {
    return original;
  }
}
