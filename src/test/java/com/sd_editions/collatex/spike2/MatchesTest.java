package com.sd_editions.collatex.spike2;

import java.util.List;

import junit.framework.TestCase;

public class MatchesTest extends TestCase {
  public void testGaps() {
    Colors colors = new Colors("a b y c z d", "a x b c n d");
    Matches matches = colors.getMatches(1, 2);
    List<Gap> gaps = matches.getGaps();
    // test x
    Gap gap1 = gaps.get(0);
    assertFalse(gap1.base.hasGap());
    assertTrue(gap1.witness.hasGap());
    assertEquals(2, gap1.witness.beginPosition);
    assertEquals(2, gap1.witness.endPosition);
    // test y
    Gap gap2 = gaps.get(1);
    assertTrue(gap2.base.hasGap());
    assertFalse(gap2.witness.hasGap());
    assertEquals(3, gap2.base.beginPosition);
    assertEquals(3, gap2.base.endPosition);
    // test z and n
    Gap gap3 = gaps.get(2);
    assertTrue(gap3.base.hasGap());
    assertTrue(gap3.witness.hasGap());
    assertEquals(5, gap3.base.beginPosition);
    assertEquals(5, gap3.base.endPosition);
    assertEquals(5, gap3.witness.beginPosition);
    assertEquals(5, gap3.witness.endPosition);
  }

  public void testGapAtTheEnd() {
    Colors colors = new Colors("a b", "a c");
    Matches matches = colors.getMatches(1, 2);
    List<Gap> gaps = matches.getGaps();
    Gap gapAtTheEnd = gaps.get(0);
    assertTrue(gapAtTheEnd.base.hasGap());
    assertTrue(gapAtTheEnd.witness.hasGap());
    assertEquals(2, gapAtTheEnd.base.beginPosition);
    assertEquals(2, gapAtTheEnd.base.endPosition);
    assertEquals(2, gapAtTheEnd.witness.beginPosition);
    assertEquals(2, gapAtTheEnd.witness.endPosition);
  }

  // TODO: test gaps of more than 1 word

}
