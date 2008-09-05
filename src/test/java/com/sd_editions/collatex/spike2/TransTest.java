package com.sd_editions.collatex.spike2;

import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import com.google.common.collect.Lists;

public class TransTest extends TestCase {

  @SuppressWarnings("boxing")
  public void testTrans() {
    Trans trans = new Trans(Lists.newArrayList(1, 2), Lists.newArrayList(2, 1));
    List<TransTuple> tuples = trans.getTuples();
    assertEquals(new TransTuple(1, 2, 1), tuples.get(0));
    assertEquals(new TransTuple(2, 1, 2), tuples.get(1));
    Set<TranspositionTuple> transpositions = trans.getTranspositions();
    assertEquals(1, transpositions.size());
  }

  public void testNoTransposition() {
    Trans trans = new Trans(Lists.newArrayList(1), Lists.newArrayList(1));
    Set<TranspositionTuple> transpositions = trans.getTranspositions();
    assertEquals(0, transpositions.size());
  }
}
