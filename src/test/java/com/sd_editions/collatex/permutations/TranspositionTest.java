package com.sd_editions.collatex.permutations;

import java.util.List;

import junit.framework.TestCase;

import com.sd_editions.collatex.permutations.collate.Transposition;

import eu.interedition.collatex.collation.CollateCore;
import eu.interedition.collatex.input.WitnessBuilder;

public class TranspositionTest extends TestCase {

  private WitnessBuilder builder;

  @Override
  protected void setUp() throws Exception {
    builder = new WitnessBuilder();
    super.setUp();
  }

  public void testTransposition1() {
    Modifications modifications = getModifications("a b c d e", "a c d b e");
    List<Transposition> transpositions = modifications.getTranspositions();
    assertEquals(2, transpositions.size());
    assertEquals("transposition: c d switches position with b", transpositions.get(0).toString());
    assertEquals("transposition: b switches position with c d", transpositions.get(1).toString());
  }

  public void testTransposition2() {
    Modifications modifications = getModifications("a b x c d ", "a c d x b");
    List<Transposition> transpositions = modifications.getTranspositions();
    assertEquals(2, transpositions.size());
    assertEquals("transposition: c d switches position with b", transpositions.get(0).toString());
    assertEquals("transposition: b switches position with c d", transpositions.get(1).toString());
  }

  public void testTransposition3() {
    Modifications modifications = getModifications("a b x c d ", "c d x a b");
    List<Transposition> transpositions = modifications.getTranspositions();
    assertEquals(2, transpositions.size());
    assertEquals("transposition: c d switches position with a b", transpositions.get(0).toString());
    assertEquals("transposition: a b switches position with c d", transpositions.get(1).toString());
  }

  public void testTranspositionOf3Groups() {
    Modifications modifications = getModifications("ab ccc d e", "d ccc e ab");
    List<Transposition> transpositions = modifications.getTranspositions();
    assertEquals(3, transpositions.size());
    assertEquals("transposition: d switches position with ab", transpositions.get(0).toString());
    assertEquals("transposition: e switches position with d", transpositions.get(1).toString());
    assertEquals("transposition: ab switches position with e", transpositions.get(2).toString());
  }

  // ab ccc d e
  // d ccc e ab
  // (1-2 -> 6-7) (3-5 -> 2-4) (6 -> 1) (7 -> 5)
  // 6 3 7 1
  // 3 2 4 1

  public void testComplex() {
    String base = "The black dog chases a red cat.";
    String witness = "A red cat chases the yellow dog";
    Modifications modifications = getModifications(base, witness);
    List<Transposition> transpositions = modifications.getTranspositions();
    assertEquals(2, transpositions.size());
    assertEquals("transposition: a red cat. switches position with The ... dog", transpositions.get(0).toString());
    assertEquals("transposition: The ... dog switches position with a red cat.", transpositions.get(1).toString());
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
    List<Modifications> permutations = new CollateCore(builder.buildWitnesses(base, witness)).compareWitness(1, 2);
    assertEquals(1, permutations.size());
    return permutations.get(0);
  }

}
