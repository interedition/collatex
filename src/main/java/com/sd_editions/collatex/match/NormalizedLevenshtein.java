package com.sd_editions.collatex.match;

import org.apache.commons.lang.StringUtils;

public class NormalizedLevenshtein implements WordDistance {

  @Override
  public float distance(String word1, String word2) {
    int levenshteinDistance = StringUtils.getLevenshteinDistance(word1.toLowerCase(), word2.toLowerCase());
    float normalizedLev = (2 * levenshteinDistance) / (word1.length() + word2.length());
    return normalizedLev;
  }
}
