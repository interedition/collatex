package com.sd_editions.collatex.spike2;

import junit.framework.TestCase;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ColorsTest extends TestCase {
  //  public void testVerySimple() {
  //    String[] witnesses = new String[] { "very simple", "simple indeed" };
  //    Colors colors = new Colors(witnesses);
  //    assertEquals(3, colors.numberOfColors());
  //  }

  @SuppressWarnings("boxing")
  public void testFirstUseCasePeter() {
    String[] witnesses = new String[] { "The black cat", "The black and white cat", "The black and green cat" };
    Colors colors = new Colors(witnesses);
    assertEquals(6, colors.numberOfColors());
    assertEquals(Sets.newHashSet(1, 2, 3), colors.getWitnessIndex(1).getWords());
    assertEquals(Sets.newHashSet(1, 2, 4, 5, 3), colors.getWitnessIndex(2).getWords());
    assertEquals(Sets.newHashSet(1, 2, 4, 6, 3), colors.getWitnessIndex(3).getWords());
    Comparison c1 = colors.compareWitness(1, 2);
    assertEquals(Lists.newArrayList("and", "white"), c1.getAddedWords());
    Comparison c2 = colors.compareWitness(1, 3);
    assertEquals(Lists.newArrayList("and", "green"), c2.getAddedWords());
    Comparison c3 = colors.compareWitness(2, 3);
    assertEquals(Lists.newArrayList("white/green"), c3.getReplacedWords());
  }

  public void testThirdUseCasePeter() {
    String[] witnesses = new String[] { "He was agast so", "He was agast", "So he was agast" };
    Colors colors = new Colors(witnesses);
    Comparison c1 = colors.compareWitness(1, 2);
    assertEquals(Lists.newArrayList("so"), c1.getRemovedWords());
    //    Comparison c2 = colors.compareWitness(1, 3);
    //    assertTrue(c2.getRemovedWords().toString(), c2.getRemovedWords().isEmpty());
    //    assertTrue(c2.getAddedWords().toString(), c2.getAddedWords().isEmpty());

  }

  //  public void testColors() {
  //    String[] witnesses = new String[] { "A black cat.", "A black dog", "One white dog" };
  //    Colors colors = new Colors(witnesses);
  //  }
  //  //  public final void testGetColorMatrixPermutations1() {
  //  //    //    Set<ColorMatrix> colorMatrixPermutations = makePermutations(new String[] { "A black cat.", "A black dog", "One white dog" });
  //  //    //    assertEquals(1, colorMatrixPermutations.size());
  //  //    //    ColorMatrix cm1 = new ColorMatrix(new int[][] { { 1, 2, 3 }, { 1, 2, 4 }, { 5, 6, 4 } });
  //  //    //    assertEquals(cm1, colorMatrixPermutations.iterator().next());
  //  //    //  }

}
