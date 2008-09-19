package com.sd_editions.collatex.spike2;

import java.util.List;

import junit.framework.TestCase;

public class ComparisonTest extends TestCase {

  public void testAddition_InTheMiddle() {
    Modifications modifications = getModifications("a cat", "a calico cat");
    assertEquals(1, modifications.size());
    assertEquals("addition: calico position: 2", modifications.get(0).toString());
  }

  public void testAddition_AtTheEnd() {
    Modifications modifications = getModifications("to be", "to be lost");
    assertEquals(1, modifications.size());
    assertEquals("addition: lost position: 3", modifications.get(0).toString());
  }

  public void testAddition_AtTheStart() {
    Modifications modifications = getModifications("to be", "not to be");
    assertEquals(1, modifications.size());
    assertEquals("addition: not position: 1", modifications.get(0).toString());
  }

  public void testOmission_InTheMiddle() {
    Modifications modifications = getModifications("a white working horse", "a horse");
    assertEquals(1, modifications.size());
    assertEquals("omission: white working position: 2", modifications.get(0).toString());
  }

  public void testOmission_AtTheStart() {
    Modifications modifications = getModifications("an almost certain death", "certain death");
    assertEquals(1, modifications.size());
    assertEquals("omission: an almost position: 1", modifications.get(0).toString());
  }

  public void testOmission_AtTheEnd() {
    Modifications modifications = getModifications("a calico, or tortoiseshell cat", "a calico");
    assertEquals(1, modifications.size());
    assertEquals("omission: or tortoiseshell cat position: 3", modifications.get(0).toString());
  }

  public void testReplacementVariantAtTheStart() {
    Modifications modifications = getModifications("black cat", "white cat");
    assertEquals(1, modifications.size());
    assertEquals("replacement: black / white position: 1", modifications.get(0).toString());
  }

  public void testReplacementVariantAtTheEnd() {
    Modifications modifications = getModifications("it's black", "it's white");
    assertEquals(1, modifications.size());
    assertEquals("replacement: black / white position: 2", modifications.get(0).toString());
  }

  // Note: transpositions and modifications are not in the same classes anymore
  //  public void testTranspositionsShouldNotBeCountedAsAdditions() {
  //    Colors colors = new Colors("a b", "b a");
  //    Comparison comparison = colors.compareWitness(1, 2);
  //    List<Modification> modifications = comparison.getModifications();
  //    assertEquals(1, modifications.size());
  //    assertEquals("transposition: a distance: 1", modifications.get(0).toString());
  //  }

  public void testPhraseAdditionAtTheStart() {
    Modifications modifications = getModifications("a b", "c d a b");
    assertEquals(1, modifications.size());
    assertEquals("addition: c d position: 1", modifications.get(0).toString());
  }

  public void testPhraseAdditionAtTheEnd() {
    Modifications modifications = getModifications("a b", "a b c d");
    assertEquals(1, modifications.size());
    assertEquals("addition: c d position: 3", modifications.get(0).toString());
  }

  public void testPhraseOmissionAtTheStart() {
    Modifications modifications = getModifications("a b c d", "c d");
    assertEquals(1, modifications.size());
    assertEquals("omission: a b position: 1", modifications.get(0).toString());
  }

  public void testPhraseOmissionAtTheEnd() {
    Modifications modifications = getModifications("a b c d", "a b");
    assertEquals(1, modifications.size());
    assertEquals("omission: c d position: 3", modifications.get(0).toString());
  }

  public void testPhraseVariantReplacementAtTheStart() {
    Modifications modifications = getModifications("a b c d", "e f g c d");
    assertEquals(1, modifications.size());
    assertEquals("replacement: a b / e f g position: 1", modifications.get(0).toString());
  }

  public void testPhraseVariantReplacementAtTheEnd() {
    Modifications modifications = getModifications("a b c d", "a b e f g");
    assertEquals(1, modifications.size());
    assertEquals("replacement: c d / e f g position: 3", modifications.get(0).toString());
  }

  public void testCombineAdditionAndRemovalToTestPositions() {
    Modifications modifications = getModifications("a b c", "c d e");
    assertEquals(2, modifications.size());
    assertEquals("omission: a b position: 1", modifications.get(0).toString());
    assertEquals("addition: d e position: 4", modifications.get(1).toString());
  }

  public void testCombineAdditionAndRemovalToTestPositionsMirrored() {
    Modifications modifications = getModifications("c d e", "a b c");
    assertEquals(2, modifications.size());
    assertEquals("addition: a b position: 1", modifications.get(0).toString());
    assertEquals("omission: d e position: 2", modifications.get(1).toString());

  }

  //  public void testTransposition() {
  //    Colors colors = new Colors("a b c d e f", "a c d b e g");
  //    Comparison comparison = colors.compareWitness(1, 2);
  //    List<Modification> modifications = comparison.getModifications();
  //    assertEquals(2, modifications.size());
  //    assertEquals("replacement: f / g position: 6", modifications.get(0).toString());
  //    assertEquals("transposition: b distance: 2", modifications.get(1).toString());
  //  }
  //
  //  public void testTransposition2() {
  //    Colors colors = new Colors("a b c", "c b a");
  //    Comparison comparison = colors.compareWitness(1, 2);
  //    List<Modification> modifications = comparison.getModifications();
  //    assertEquals(2, modifications.size());
  //    assertEquals("transposition: a distance: 2", modifications.get(0).toString());
  //    assertEquals("transposition: c distance: 2", modifications.get(0).toString());
  //  }

  private Modifications getModifications(String base, String witness) {
    List<Modifications> permutations = new Colors(base, witness).compareWitness(1, 2);
    assertEquals(1, permutations.size());
    return permutations.get(0);
  }

}
