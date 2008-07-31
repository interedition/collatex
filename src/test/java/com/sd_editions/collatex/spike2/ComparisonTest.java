package com.sd_editions.collatex.spike2;

import java.util.List;

import junit.framework.TestCase;

public class ComparisonTest extends TestCase {

  public void testAddition_InTheMiddle() {
    Colors colors = new Colors("a cat", "a calico cat");
    Comparison comparison = colors.compareWitness(1, 2);
    List<Modification> modifications = comparison.getModifications();
    assertEquals(1, modifications.size());
    assertEquals("addition: calico position: 1", modifications.get(0).toString());
  }

  public void testAddition_AtTheEnd() {
    Colors colors = new Colors("to be", "to be lost");
    Comparison comparison = colors.compareWitness(1, 2);
    List<Modification> modifications = comparison.getModifications();
    assertEquals(1, modifications.size());
    assertEquals("addition: lost position: 2", modifications.get(0).toString());
  }

  public void testAddition_AtTheStart() {
    Colors colors = new Colors("to be", "not to be");
    Comparison comparison = colors.compareWitness(1, 2);
    List<Modification> modifications = comparison.getModifications();
    assertEquals(1, modifications.size());
    assertEquals("addition: not position: 0", modifications.get(0).toString());
  }

  //  public void testReplacementVariant() {
  //    Colors colors = new Colors("black cat", "white cat");
  //    //    Comparison comparison = colors.compareWitness(1, 2);
  //    //    List<PositionTuple> matches = comparison.getMatches();
  //    //    List<PositionTuple> expected = Lists.newArrayList(new PositionTuple(1, 1));
  //    //    assertEquals(expected, matches);
  //  }

}
