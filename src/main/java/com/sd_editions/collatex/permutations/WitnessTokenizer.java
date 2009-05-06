package com.sd_editions.collatex.permutations;

import java.util.regex.Pattern;

import com.sd_editions.collatex.iterator.ArrayIterator;

public class WitnessTokenizer {
  private final ArrayIterator iterator;
  private final boolean normalize;
  private final static Pattern SPLITTER = Pattern.compile("\\s+");

  public WitnessTokenizer(String witness, boolean _normalize) {
    this.normalize = _normalize;
    String[] tokens = SPLITTER.split(witness.trim());
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
