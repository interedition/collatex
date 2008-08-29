package com.sd_editions.collatex.spike2;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.google.common.collect.Lists;

public class TranspositionTest extends TestCase {
  @SuppressWarnings("boxing")
  public void testSequenceExpectations() {
    List<Integer> sequence = Lists.newArrayList(1, 2, 3, 4, 5);
    Map<Integer, Integer> sequenceExpectations = TranspositionDetection.calculateSequenceExpectations(sequence);
    assertEquals(new Integer(1), sequenceExpectations.get(2));
    assertEquals(new Integer(2), sequenceExpectations.get(3));
    assertEquals(new Integer(3), sequenceExpectations.get(4));
    assertEquals(new Integer(4), sequenceExpectations.get(5));
  }

  public void testPhrases1() {
    Colors colors = new Colors("a b c d e", "a c d b e");
    TranspositionDetection detection = colors.detectTranspositions(1, 2);
    List<Phrase> phrases = detection.getPhrases();
    assertEquals(4, phrases.size());
    assertEquals("a", phrases.get(0).toString());
    assertEquals("c d", phrases.get(1).toString());
    assertEquals("b", phrases.get(2).toString());
    assertEquals("e", phrases.get(3).toString());
    //    assertEquals("transposition: b switches position with c d", modifications.get(0).toString());
  }

  public void testPhrases2() {
    Colors colors = new Colors("a b x c d ", "a c d x b");
    TranspositionDetection detection = colors.detectTranspositions(1, 2);
    List<Phrase> phrases = detection.getPhrases();
    assertEquals(4, phrases.size());
    assertEquals("a", phrases.get(0).toString());
    assertEquals("c d", phrases.get(1).toString());
    assertEquals("x", phrases.get(2).toString());
    assertEquals("b", phrases.get(3).toString());
    //    assertEquals("transposition: b switches position with c d", modifications.get(0).toString());
  }

  public void testPhrases3() {
    Colors colors = new Colors("a b x c d ", "c d x a b");
    TranspositionDetection detection = colors.detectTranspositions(1, 2);
    List<Phrase> phrases = detection.getPhrases();
    assertEquals(3, phrases.size());
    assertEquals("c d", phrases.get(0).toString());
    assertEquals("x", phrases.get(1).toString());
    assertEquals("a b", phrases.get(2).toString());
    //    assertEquals("transposition: a b switches position with c d", modifications.get(0).toString());
  }

}
