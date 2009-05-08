package com.sd_editions.collatex.match.worddistance;

import junit.framework.TestCase;

import org.junit.Test;

import com.sd_editions.collatex.Block.Util;

public class NormalizedLevenshteinTest extends TestCase {
  @Test
  public void testNormalizedLevenshtein() {
    NormalizedLevenshtein normalizedLevenshtein = new NormalizedLevenshtein();
    Levenshtein levenshtein = new Levenshtein();

    Util.p(levenshtein.distance("this", "those"));
    Util.p(normalizedLevenshtein.distance("this", "those"));

    Util.p(levenshtein.distance("glass", "glasses"));
    Util.p(normalizedLevenshtein.distance("glass", "glasses"));

    Util.p(levenshtein.distance("glasses", "plates"));
    Util.p(normalizedLevenshtein.distance("glasses", "plates"));
  }
}
