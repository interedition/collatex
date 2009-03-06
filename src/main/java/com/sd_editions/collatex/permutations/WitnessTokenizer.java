package com.sd_editions.collatex.permutations;

import com.sd_editions.collatex.iterator.ArrayIterator;

public class WitnessTokenizer {
  private final ArrayIterator iterator;
  private final boolean normalize;

  public WitnessTokenizer(String witness, boolean _normalize) {
    this.normalize = _normalize;
    String[] tokens = witness.split("\\s+");
    iterator = new ArrayIterator(tokens);
  }

  public boolean hasNextToken() {
    return iterator.hasNext();
  }

  public String nextToken() {
    String token = (String) iterator.next();
    if (normalize) {
      token = token.replaceAll("\\p{Punct}", "").toLowerCase();
    }
    return token;
  }
}
