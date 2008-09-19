package com.sd_editions.collatex.spike2;

import java.util.List;

import junit.framework.TestCase;

import com.sd_editions.collatex.spike2.collate.Transposition;

public class TranspositionTest extends TestCase {
  public void testTransposition1() {
    Modifications modifications = getModifications("a b c d e", "a c d b e");
    List<Transposition> transpositions = modifications.getTranspositions();
    assertEquals(1, transpositions.size());
    assertEquals("transposition: b switches position with c d", transpositions.get(0).toString());
  }

  public void testTransposition2() {
    Modifications modifications = getModifications("a b x c d ", "a c d x b");
    List<Transposition> transpositions = modifications.getTranspositions();
    assertEquals(1, transpositions.size());
    assertEquals("transposition: b switches position with c d", transpositions.get(0).toString());
  }

  public void testTransposition3() {
    Modifications modifications = getModifications("a b x c d ", "c d x a b");
    List<Transposition> transpositions = modifications.getTranspositions();
    assertEquals(1, transpositions.size());
    assertEquals("transposition: a b switches position with c d", transpositions.get(0).toString());
  }

  //Note: this test should lead to a transposition :-)
  public void testComplex() {
    String base = "The black dog chases a red cat.";
    String witness = "A red cat chases the yellow dog";
    Modifications modifications = getModifications(base, witness);
    List<Transposition> transpositions = modifications.getTranspositions();
    assertEquals(1, transpositions.size());
    // TODO: add expected Transposition
  }

  //

  //  public void testPhrases1() {
  //    Colors colors = new Colors("a b c d e", "a c d b e");
  //    TranspositionDetection detection = colors.detectTranspositions(1, 2);
  //    List<Phrase> phrases = detection.getPhrases();
  //    assertEquals(4, phrases.size());
  //    assertEquals("a", phrases.get(0).toString());
  //    assertEquals("c d", phrases.get(1).toString());
  //    assertEquals("b", phrases.get(2).toString());
  //    assertEquals("e", phrases.get(3).toString());
  //    
  //  }
  //
  //  public void testPhrases2() {
  //    Colors colors = new Colors("a b x c d ", "a c d x b");
  //    TranspositionDetection detection = colors.detectTranspositions(1, 2);
  //    List<Phrase> phrases = detection.getPhrases();
  //    assertEquals(4, phrases.size());
  //    assertEquals("a", phrases.get(0).toString());
  //    assertEquals("c d", phrases.get(1).toString());
  //    assertEquals("x", phrases.get(2).toString());
  //    assertEquals("b", phrases.get(3).toString());
  //    //    assertEquals("transposition: b switches position with c d", modifications.get(0).toString());
  //  }
  //
  //  public void testPhrases3() {
  //    Colors colors = new Colors("a b x c d ", "c d x a b");
  //    TranspositionDetection detection = colors.detectTranspositions(1, 2);
  //    List<Phrase> phrases = detection.getPhrases();
  //    assertEquals(3, phrases.size());
  //    assertEquals("c d", phrases.get(0).toString());
  //    assertEquals("x", phrases.get(1).toString());
  //    assertEquals("a b", phrases.get(2).toString());
  //    //    assertEquals("transposition: a b switches position with c d", modifications.get(0).toString());
  //  }
  private Modifications getModifications(String base, String witness) {
    List<Modifications> permutations = new Colors(base, witness).compareWitness(1, 2);
    assertEquals(1, permutations.size());
    return permutations.get(0);
  }

}
