package com.sd_editions.collatex.spike2;

import java.util.List;

import junit.framework.TestCase;

public class MatchesTest extends TestCase {
  public void testGaps() {
    Colors colors = new Colors("a b y c z d", "a x b c n d");
    Comparison compareWitness = colors.compareWitness(1, 2);
    Matches matches = compareWitness.getMatches();
    List<Gap> gaps = matches.getGaps();
    // we assume that empty gaps are possible at the beginning and the end..
    // to keep the algorithms simple... we could always filter away empty gaps later
    Gap gap0 = gaps.get(0);
    assertEquals(0, gap0.distanceBase);
    assertEquals(0, gap0.distanceWitness);
    // test x
    Gap gap1 = gaps.get(1);
    assertEquals(0, gap1.distanceBase);
    assertEquals(1, gap1.distanceWitness);
    assertEquals(2, gap1.witnessBeginPosition);
    assertEquals(2, gap1.witnessEndPosition);
    // test y
    Gap gap2 = gaps.get(2);
    assertEquals(1, gap2.distanceBase);
    assertEquals(0, gap2.distanceWitness);
    assertEquals(3, gap2.baseBeginPosition);
    assertEquals(3, gap2.baseEndPosition);
    // test z and n
    Gap gap3 = gaps.get(3);
    assertEquals(1, gap3.distanceBase);
    assertEquals(1, gap3.distanceWitness);
    assertEquals(5, gap3.baseBeginPosition);
    assertEquals(5, gap3.baseEndPosition);
    assertEquals(5, gap3.witnessBeginPosition);
    assertEquals(5, gap3.witnessEndPosition);
    // empty gap at the end
    Gap gap4 = gaps.get(4);
    assertEquals(0, gap4.distanceBase);
    assertEquals(0, gap4.distanceWitness);
  }

  public void testGapAtTheEnd() {
    Colors colors = new Colors("a b", "a c");
    Comparison compareWitness = colors.compareWitness(1, 2);
    Matches matches = compareWitness.getMatches();
    List<Gap> gaps = matches.getGaps();
    Gap gapAtTheEnd = gaps.get(1);
    assertEquals(1, gapAtTheEnd.distanceBase);
    assertEquals(1, gapAtTheEnd.distanceWitness);
    assertEquals(2, gapAtTheEnd.baseBeginPosition);
    assertEquals(2, gapAtTheEnd.baseEndPosition);
    assertEquals(2, gapAtTheEnd.witnessBeginPosition);
    assertEquals(2, gapAtTheEnd.witnessEndPosition);
  }

  // TODO: test gaps of more than 1 word

}
