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
    assertEquals("omission: white", modifications.get(0).toString());
    //    assertEquals("omission: working", modifications.get(1).toString());
  }

  public void testOmission_AtTheStart() {
    Colors colors = new Colors("an almost certain death", "certain death");
    Comparison comparison = colors.compareWitness(1, 2);
    List<Modification> modifications = comparison.getModifications();
    assertEquals(1, modifications.size());
    assertEquals("omission: an", modifications.get(0).toString());
    //    assertEquals("omission: almost", modifications.get(1).toString());
  }

  public void testOmission_AtTheEnd() {
    Colors colors = new Colors("a calico, or tortoiseshell cat", "a calico");
    Comparison comparison = colors.compareWitness(1, 2);
    List<Modification> modifications = comparison.getModifications();
    assertEquals(1, modifications.size());
    assertEquals("omission: or", modifications.get(0).toString());
    //    assertEquals("omission: tortoiseshell", modifications.get(0).toString());
    //    assertEquals("omission: cat", modifications.get(0).toString());
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

}
