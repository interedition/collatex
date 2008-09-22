package com.sd_editions.collatex.spike2;

import org.apache.commons.lang.StringUtils;

public class Word {
  private final String original;
  public String normalized;
  final int position;

  public Word(String _original, int _position) {
    this.original = _original;
    this.normalized = original.toLowerCase().replaceAll("\\W", "");
    this.position = _position;
  }

  public float distanceTo(Word otherWord) {
    String w1 = normalized;
    String w2 = otherWord.normalized;
    float lev = StringUtils.getLevenshteinDistance(w1, w2);
    float normalizedLev = (2 * lev) / (w1.length() + w2.length());
    return normalizedLev;
  }

  @Override
  public String toString() {
    return original;
  }

}
