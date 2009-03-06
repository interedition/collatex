package com.sd_editions.collatex.match;

import java.util.HashSet;
import java.util.Set;

import com.sd_editions.collatex.match.ColorMatrix;

import junit.framework.TestCase;

public class ColorMatrixTest extends TestCase {

  private ColorMatrix testColorMatrix;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    testColorMatrix = new ColorMatrix(2, 3);
  }

  @SuppressWarnings("boxing")
  public void testSetGetCell() {
    assertEquals(0, testColorMatrix.getCell(0, 0));
    testColorMatrix.setCell(0, 0, 1);
    assertEquals(1, testColorMatrix.getCell(0, 0));
  }

  public void testEquals0() {
    Object o = new Object();
    assertFalse(testColorMatrix.equals(o));
  }

  public void testEquals1() {
    ColorMatrix m2 = new ColorMatrix(1, 1);
    assertFalse(testColorMatrix.equals(m2));
  }

  public void testEquals2() {
    ColorMatrix m2 = new ColorMatrix(2, 1);
    assertFalse(testColorMatrix.equals(m2));
  }

  public void testEquals3() {
    ColorMatrix m2 = new ColorMatrix(1, 2);
    assertFalse(testColorMatrix.equals(m2));
  }

  public void testEquals4() {
    ColorMatrix m2 = new ColorMatrix(2, 3);
    assertTrue(testColorMatrix.equals(m2));
    testColorMatrix.setCell(0, 0, 1);
    assertFalse(testColorMatrix.equals(m2));
    m2.setCell(0, 0, 1);
    assertTrue(testColorMatrix.equals(m2));
  }

  public void testEquals5() {
    testColorMatrix.setCell(1, 2, 2);
    ColorMatrix m2 = new ColorMatrix(testColorMatrix);
    assertTrue(testColorMatrix.equals(m2));
    m2.setCell(0, 0, 1);
    assertFalse(testColorMatrix.equals(m2));
  }

  public void testColorMatrixSet() {
    Set<ColorMatrix> set = new HashSet<ColorMatrix>();
    set.add(testColorMatrix);
    ColorMatrix m2 = new ColorMatrix(testColorMatrix);
    set.add(m2);
    assertEquals(testColorMatrix.hashCode(), m2.hashCode());
    assertEquals(1, set.size());
  }

  public void testToString() {
    assertEquals("|  0  0  0 |\n|  0  0  0 |\n", testColorMatrix.toString());
  }
}
