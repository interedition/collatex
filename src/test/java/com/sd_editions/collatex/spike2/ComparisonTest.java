package com.sd_editions.collatex.spike2;

import java.util.List;

import junit.framework.TestCase;

public class ComparisonTest extends TestCase {

  public void testAddition_InTheMiddle() {
    Colors colors = new Colors("a cat", "a calico cat");
    Comparison comparison = colors.compareWitness(1, 2);
    List<Modification> modifications = comparison.getModifications();
    assertEquals(1, modifications.size());
    assertEquals("addition: calico position: 2", modifications.get(0).toString());
  }

  public void testAddition_AtTheEnd() {
    Colors colors = new Colors("to be", "to be lost");
    Comparison comparison = colors.compareWitness(1, 2);
    List<Modification> modifications = comparison.getModifications();
    assertEquals(1, modifications.size());
    assertEquals("addition: lost position: 3", modifications.get(0).toString());
  }

  public void testAddition_AtTheStart() {
    Colors colors = new Colors("to be", "not to be");
    Comparison comparison = colors.compareWitness(1, 2);
    List<Modification> modifications = comparison.getModifications();
    assertEquals(1, modifications.size());
    assertEquals("addition: not position: 1", modifications.get(0).toString());
  }

  public void testOmission_InTheMiddle() {
    Colors colors = new Colors("a white working horse", "a horse");
    Comparison comparison = colors.compareWitness(1, 2);
    List<Modification> modifications = comparison.getModifications();
    assertEquals(1, modifications.size());
    assertEquals("omission: white working position: 2", modifications.get(0).toString());
  }

  public void testOmission_AtTheStart() {
    Colors colors = new Colors("an almost certain death", "certain death");
    Comparison comparison = colors.compareWitness(1, 2);
    List<Modification> modifications = comparison.getModifications();
    assertEquals(1, modifications.size());
    assertEquals("omission: an almost position: 1", modifications.get(0).toString());
  }

  public void testOmission_AtTheEnd() {
    Colors colors = new Colors("a calico, or tortoiseshell cat", "a calico");
    Comparison comparison = colors.compareWitness(1, 2);
    List<Modification> modifications = comparison.getModifications();
    assertEquals(1, modifications.size());
    assertEquals("omission: or tortoiseshell cat position: 3", modifications.get(0).toString());
  }

  public void testReplacementVariantAtTheStart() {
    Colors colors = new Colors("black cat", "white cat");
    Comparison comparison = colors.compareWitness(1, 2);
    List<Modification> modifications = comparison.getModifications();
    assertEquals(1, modifications.size());
    assertEquals("replacement: black / white position: 1", modifications.get(0).toString());
  }

  public void testReplacementVariantAtTheEnd() {
    Colors colors = new Colors("it's black", "it's white");
    Comparison comparison = colors.compareWitness(1, 2);
    List<Modification> modifications = comparison.getModifications();
    assertEquals(1, modifications.size());
    assertEquals("replacement: black / white position: 2", modifications.get(0).toString());
  }

  public void testTranspositionsShouldNotBeCountedAsAdditions() {
    Colors colors = new Colors("a b", "b a");
    Comparison comparison = colors.compareWitness(1, 2);
    List<Modification> modifications = comparison.getModifications();
    assertEquals(0, modifications.size());
  }

  public void testPhraseAdditionAtTheStart() {
    Colors colors = new Colors("a b", "c d a b");
    Comparison comparison = colors.compareWitness(1, 2);
    List<Modification> modifications = comparison.getModifications();
    assertEquals(1, modifications.size());
    assertEquals("addition: c d position: 1", modifications.get(0).toString());
  }

  public void testPhraseAdditionAtTheEnd() {
    Colors colors = new Colors("a b", "a b c d");
    Comparison comparison = colors.compareWitness(1, 2);
    List<Modification> modifications = comparison.getModifications();
    assertEquals(1, modifications.size());
    assertEquals("addition: c d position: 3", modifications.get(0).toString());
  }

  public void testPhraseOmissionAtTheStart() {
    Colors colors = new Colors("a b c d", "c d");
    Comparison comparison = colors.compareWitness(1, 2);
    List<Modification> modifications = comparison.getModifications();
    assertEquals(1, modifications.size());
    assertEquals("omission: a b position: 1", modifications.get(0).toString());
  }

  public void testPhraseOmissionAtTheEnd() {
    Colors colors = new Colors("a b c d", "a b");
    Comparison comparison = colors.compareWitness(1, 2);
    List<Modification> modifications = comparison.getModifications();
    assertEquals(1, modifications.size());
    assertEquals("omission: c d position: 3", modifications.get(0).toString());
  }

  public void testCombineAdditionAndRemovalToTestPositions() {
    Colors colors = new Colors("a b c", "c d e");
    Comparison comparison = colors.compareWitness(1, 2);
    List<Modification> modifications = comparison.getModifications();
    assertEquals(2, modifications.size());
    assertEquals("omission: a b position: 1", modifications.get(0).toString());
    assertEquals("addition: d e position: 4", modifications.get(1).toString());
  }

  public void testCombineAdditionAndRemovalToTestPositionsMirrored() {
    Colors colors = new Colors("c d e", "a b c");
    Comparison comparison = colors.compareWitness(1, 2);
    List<Modification> modifications = comparison.getModifications();
    assertEquals(2, modifications.size());
    assertEquals("addition: a b position: 1", modifications.get(0).toString());
    assertEquals("omission: d e position: 2", modifications.get(1).toString());
  }

}
