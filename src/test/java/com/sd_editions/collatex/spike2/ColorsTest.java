package com.sd_editions.collatex.spike2;

import junit.framework.TestCase;

public class ColorsTest extends TestCase {
  public void testVerySimple() {
    String[] witnesses = new String[] { "very simple", "simple indeed" };
    Colors colors = new Colors(witnesses);
    assertEquals(3, colors.numberOfColors());
  }

  public void testFirstUseCasePeter() {
    String[] witnesses = new String[] { "The black cat", "The black and white cat", "The black and green cat" };
    Colors colors = new Colors(witnesses);
    assertEquals(6, colors.numberOfColors());
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
