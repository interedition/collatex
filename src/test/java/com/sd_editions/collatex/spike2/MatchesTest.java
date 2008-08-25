package com.sd_editions.collatex.spike2;

import java.util.List;

import junit.framework.TestCase;

public class MatchesTest extends TestCase {
  public void testGaps() {
    Colors colors = new Colors("a b y c z d", "a x b c n d");
    Matches matches = colors.getMatches(1, 2);
    List<Gap> gaps = matches.getGaps();
    // we assume that empty gaps are possible at the beginning and the end..
    // to keep the algorithms simple... we could always filter away empty gaps later
    Gap gap0 = gaps.get(0);
    assertFalse(gap0.gapInBase());
    assertFalse(gap0.gapInWitness());
    // test x
    Gap gap1 = gaps.get(1);
    assertFalse(gap1.gapInBase());
    assertTrue(gap1.gapInWitness());
    assertEquals(2, gap1.witnessBeginPosition);
    assertEquals(2, gap1.witnessEndPosition);
    // test y
    Gap gap2 = gaps.get(2);
    assertTrue(gap2.gapInBase());
    assertFalse(gap2.gapInWitness());
    assertEquals(3, gap2.baseBeginPosition);
    assertEquals(3, gap2.baseEndPosition);
    // test z and n
    Gap gap3 = gaps.get(3);
    assertTrue(gap3.gapInBase());
    assertTrue(gap3.gapInWitness());
    assertEquals(5, gap3.baseBeginPosition);
    assertEquals(5, gap3.baseEndPosition);
    assertEquals(5, gap3.witnessBeginPosition);
    assertEquals(5, gap3.witnessEndPosition);
    // empty gap at the end
    Gap gap4 = gaps.get(4);
    assertFalse(gap4.gapInBase());
    assertFalse(gap4.gapInWitness());
  }

  public void testGapAtTheEnd() {
    Colors colors = new Colors("a b", "a c");
    Matches matches = colors.getMatches(1, 2);
    List<Gap> gaps = matches.getGaps();
    Gap gapAtTheEnd = gaps.get(1);
    assertTrue(gapAtTheEnd.gapInBase());
    assertTrue(gapAtTheEnd.gapInWitness());
    assertEquals(2, gapAtTheEnd.baseBeginPosition);
    assertEquals(2, gapAtTheEnd.baseEndPosition);
    assertEquals(2, gapAtTheEnd.witnessBeginPosition);
    assertEquals(2, gapAtTheEnd.witnessEndPosition);
  }

  // TODO: test gaps of more than 1 word

}
