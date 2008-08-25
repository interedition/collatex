package com.sd_editions.collatex.spike2;

import java.util.List;

import junit.framework.TestCase;

public class MatchesTest extends TestCase {
  public void testMismatches() {
    Colors colors = new Colors("a b y c z d", "a x b c n d");
    Matches matches = colors.getMatches(1, 2);
    List<MisMatch> mismatches = matches.getMismatches();
    // test x
    MisMatch mismatch1 = mismatches.get(0);
    assertFalse(mismatch1.base.hasGap());
    assertTrue(mismatch1.witness.hasGap());
    assertEquals(2, mismatch1.witness.beginPosition);
    assertEquals(2, mismatch1.witness.endPosition);
    // test y
    MisMatch mismatch2 = mismatches.get(1);
    assertTrue(mismatch2.base.hasGap());
    assertFalse(mismatch2.witness.hasGap());
    assertEquals(3, mismatch2.base.beginPosition);
    assertEquals(3, mismatch2.base.endPosition);
    // test z and n
    MisMatch mismatch3 = mismatches.get(2);
    assertTrue(mismatch3.base.hasGap());
    assertTrue(mismatch3.witness.hasGap());
    assertEquals(5, mismatch3.base.beginPosition);
    assertEquals(5, mismatch3.base.endPosition);
    assertEquals(5, mismatch3.witness.beginPosition);
    assertEquals(5, mismatch3.witness.endPosition);
  }

  public void testMismatchAtTheEnd() {
    Colors colors = new Colors("a b", "a c");
    Matches matches = colors.getMatches(1, 2);
    List<MisMatch> mismatches = matches.getMismatches();
    MisMatch mismatchAtTheEnd = mismatches.get(0);
    assertTrue(mismatchAtTheEnd.base.hasGap());
    assertTrue(mismatchAtTheEnd.witness.hasGap());
    assertEquals(2, mismatchAtTheEnd.base.beginPosition);
    assertEquals(2, mismatchAtTheEnd.base.endPosition);
    assertEquals(2, mismatchAtTheEnd.witness.beginPosition);
    assertEquals(2, mismatchAtTheEnd.witness.endPosition);
  }

  // TODO: test gaps of more than 1 word

}
