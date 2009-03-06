package com.sd_editions.collatex.permutations;

import org.apache.commons.lang.StringUtils;

public class Word {
  public final String original;
  public final String normalized;
  public final int position;

  public Word(String _original, int _position) {
    this.original = _original;
    this.normalized = original.toLowerCase().replaceAll("[`~'!@#$%^&*():;,\\.]", "");
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

  @Override
  public int hashCode() {
    return original.hashCode() * (10 ^ position);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Word)) return false;
    Word other = (Word) obj;
    return other.original.equals(this.original) && (other.position == this.position);
  }
}
