package com.sd_editions.collatex.spike2;

import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

public class TransTest extends TestCase {

  public void testTrans() {
    Trans trans = new Trans(new Integer[] { 1, 2 }, new Integer[] { 2, 1 });
    List<TransTuple> tuples = trans.getTuples();
    assertEquals(new TransTuple(1, 2, 1), tuples.get(0));
    assertEquals(new TransTuple(2, 1, 2), tuples.get(1));
    Set<Transposition> transpositions = trans.getTranspositions();
    assertEquals(1, transpositions.size());
  }
}
