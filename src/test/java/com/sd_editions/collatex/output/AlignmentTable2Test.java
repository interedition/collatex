package com.sd_editions.collatex.output;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.sd_editions.collatex.permutations.Witness;
import com.sd_editions.collatex.permutations.WitnessBuilder;

public class AlignmentTable2Test {
  @Test
  public void testCreateSuperBase() {
    WitnessBuilder builder = new WitnessBuilder();
    Witness a = builder.build("A", "the first witness");
    AlignmentTable2 alignmentTable = new AlignmentTable2();
    alignmentTable.addFirstWitness(a);
    Witness superBase = alignmentTable.createSuperBase();
    assertEquals("the first witness", superBase.toString());
  }

  @Test
  public void testStringOutputOneWitness() {
    WitnessBuilder builder = new WitnessBuilder();
    Witness w1 = builder.build("A", "the black cat");
    AlignmentTable2 table = new AlignmentTable2();
    table.addFirstWitness(w1);
    String expected = "A: the|black|cat";
    assertEquals(expected, table.toString());
  }

  // TODO: test empty witness
  @Test
  public void testStringOutputTwoWitnesses() {
    WitnessBuilder builder = new WitnessBuilder();
    Witness w1 = builder.build("A", "the black cat");
    Witness w2 = builder.build("B", "the black cat");
    AlignmentTable2 table = new AlignmentTable2();
    table.addFirstWitness(w1);
    // TODO: word contains id also, which refers to Witness
    table.addMatch(w2, w2.getWordOnPosition(1));
    table.addMatch(w2, w2.getWordOnPosition(2));
    table.addMatch(w2, w2.getWordOnPosition(3));
    String expected = "A: the|black|cat\n";
    expected += "B: the|black|cat";
    assertEquals(expected, table.toString());
  }
}
