package eu.interedition.collatex2.implementation.vg_alignment;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IWitness;

public class AlignmentTranspositionTest {
  private static CollateXEngine factory;

  @BeforeClass
  public static void setup() {
    factory = new CollateXEngine();
  }
  
  @Test
  public void testTransposition1() {
    IWitness a = factory.createWitness("A", "the white and black cat");
    IWitness b = factory.createWitness("B", "The black cat");
    IWitness c = factory.createWitness("C", "the black and white cat");
    IWitness d = factory.createWitness("D", "the black and green cat");
    IAlignmentTable table = factory.align(a, b, c, d);
    assertEquals("A: |the|white|and|black|cat|", table.getRow(a).toString());
    assertEquals("B: |The| | |black|cat|", table.getRow(b).toString());
    assertEquals("C: |the|black|and|white|cat|", table.getRow(c).toString());
    assertEquals("D: |the|black|and|green|cat|", table.getRow(d).toString());
  }
  
  @Test
  public void testTransposition2() {
    IWitness a = factory.createWitness("A", "He was agast, so");
    IWitness b = factory.createWitness("B", "He was agast");
    IWitness c = factory.createWitness("C", "So he was agast");
    IAlignmentTable table = factory.align(a, b, c);
    assertEquals("A: | |He|was|agast,|so|", table.getRow(a).toString());
    assertEquals("B: | |He|was|agast| |", table.getRow(b).toString());
    assertEquals("C: |So|he|was|agast| |", table.getRow(c).toString());
  }

  //TODO: it would be nice if He was agast stayed in one place!
  @Test
  public void testTransposition2Reordered() {
    IWitness a = factory.createWitness("A", "So he was agast");
    IWitness b = factory.createWitness("B", "He was agast");
    IWitness c = factory.createWitness("C", "He was agast, so");
    IAlignmentTable table = factory.align(a, b, c);
    assertEquals("A: | | | |So|he|was|agast|", table.getRow(a).toString());
    assertEquals("B: | | | | |He|was|agast|", table.getRow(b).toString());
    assertEquals("C: |He|was|agast,|so| | | |", table.getRow(c).toString());
  }

}
