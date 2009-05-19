package com.sd_editions.collatex.output;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import com.sd_editions.collatex.permutations.Witness;
import com.sd_editions.collatex.permutations.WitnessBuilder;

public class MagicClass2Test {
  private static WitnessBuilder builder;

  @BeforeClass
  public static void setUp() {
    builder = new WitnessBuilder();
  }

  @Test
  public void testEverythingMatches() {
    Witness w1 = builder.build("A", "the black cat");
    Witness w2 = builder.build("B", "the black cat");
    Witness w3 = builder.build("C", "the black cat");
    MagicClass2 magic = new MagicClass2(w1, w2, w3);
    AlignmentTable2 table = magic.createAlignmentTable();
    String expected = "A: the|black|cat\n";
    expected += "B: the|black|cat\n";
    expected += "C: the|black|cat";
    assertEquals(expected, table.toString());
  }

  @Test
  public void testOmission() {
    Witness w1 = builder.build("A", "the black cat");
    Witness w2 = builder.build("B", "the cat");
    Witness w3 = builder.build("C", "the black cat");
    MagicClass2 magic = new MagicClass2(w1, w2, w3);
    AlignmentTable2 table = magic.createAlignmentTable();
    String expected = "A: the|black|cat\n";
    expected += "B: the| |cat\n";
    expected += "C: the|black|cat";
    assertEquals(expected, table.toString());
  }

  // this is an addition.. not the easiest test
  //  @Test
  //  public void testTwoWitnesses() {
  //    Witness w1 = builder.build("A", "the black cat");
  //    Witness w2 = builder.build("B", "the white and black cat");
  //    MagicClass2 magic = new MagicClass2(w1, w2);
  //    AlignmentTable2 table = magic.createAlignmentTable();
  //    String expected = "A: the| | |black|cat";
  //    expected += "B: the|white|and|black|cat";
  //    System.out.println(table.toString());
  //  }

}
