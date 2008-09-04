package com.sd_editions.collatex.spike2;

import java.util.Set;

import junit.framework.TestCase;

public class TransTest extends TestCase {

  public void testTrans() {
    Trans trans = new Trans(new Integer[] { 1, 2 }, new Integer[] { 2, 1 });
    TransTuple[] tuples = trans.getTuples();
    assertEquals(new TransTuple(1, 2, 1), tuples[0]);
    assertEquals(new TransTuple(2, 1, 2), tuples[1]);
    Set<TransTuple2> transpositions = trans.getTranspositions();
    assertEquals(1, transpositions.size());
  }

}
