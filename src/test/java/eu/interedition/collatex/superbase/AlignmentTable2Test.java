package eu.interedition.collatex.superbase;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import eu.interedition.collatex.alignment.multiple_witness.AlignmentTable2;
import eu.interedition.collatex.alignment.multiple_witness.AlignmentTableCreator;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.WitnessSet;
import eu.interedition.collatex.input.builders.WitnessBuilder;

public class AlignmentTable2Test {
  @Test
  public void testCreateSuperBase() {
    WitnessBuilder builder = new WitnessBuilder();
    Segment a = builder.build("A", "the first witness");
    WitnessSet set = new WitnessSet(a);
    AlignmentTable2 table = AlignmentTableCreator.createAlignmentTable(set);
    Segment superbase = table.createSuperbase();
    assertEquals("the first witness", superbase.toString());
  }

  @Test
  public void testCreateSuperBaseWithVariation() {
    WitnessBuilder builder = new WitnessBuilder();
    Segment a = builder.build("A", "the first witness");
    Segment b = builder.build("B", "the second witness");
    WitnessSet set = new WitnessSet(a, b);
    AlignmentTable2 table = AlignmentTableCreator.createAlignmentTable(set);
    Segment superbase = table.createSuperbase();
    assertEquals("the first second witness", superbase.toString());
  }

  @Test
  public void testStringOutputOneWitness() {
    WitnessBuilder builder = new WitnessBuilder();
    Segment a = builder.build("A", "the black cat");
    WitnessSet set = new WitnessSet(a);
    AlignmentTable2 table = AlignmentTableCreator.createAlignmentTable(set);
    String expected = "A: the|black|cat\n";
    assertEquals(expected, table.toString());
  }

  @Test
  public void testStringOutputTwoWitnesses() {
    WitnessBuilder builder = new WitnessBuilder();
    Segment a = builder.build("A", "the black cat");
    Segment b = builder.build("B", "the black cat");
    WitnessSet set = new WitnessSet(a, b);
    AlignmentTable2 table = AlignmentTableCreator.createAlignmentTable(set);
    // TODO: add match test can be moved to a column test class? 
    //    // TODO: word contains id also, which refers to Witness
    //    Column c1 = table.getColumns().get(0);
    //    Column c2 = table.getColumns().get(1);
    //    Column c3 = table.getColumns().get(2);
    //    table.addMatch(w2, w2.getWordOnPosition(1), c1);
    //    table.addMatch(w2, w2.getWordOnPosition(2), c2);
    //    table.addMatch(w2, w2.getWordOnPosition(3), c3);
    String expected = "A: the|black|cat\n";
    expected += "B: the|black|cat\n";
    assertEquals(expected, table.toString());
  }

  @Test
  public void testStringOutputEmptyCells() {
    WitnessBuilder builder = new WitnessBuilder();
    Segment a = builder.build("A", "the black cat");
    Segment b = builder.build("B", "the");
    WitnessSet set = new WitnessSet(a, b);
    AlignmentTable2 table = AlignmentTableCreator.createAlignmentTable(set);
    // TODO: add match test can be moved to column class?
    //    Column column = table.getColumns().get(0);
    // TODO: word contains id also, which refers to Witness
    //table.addMatch(w2, w2.getWordOnPosition(1), column);
    String expected = "A: the|black|cat\n";
    expected += "B: the| | \n";
    assertEquals(expected, table.toString());
  }

  @Test
  public void testTranspositionsAreNotStoredInAlignmentTable() {
    WitnessBuilder builder = new WitnessBuilder();
    Segment a = builder.build("A", "the black and white cat");
    Segment b = builder.build("B", "the white and black cat");
    WitnessSet set = new WitnessSet(a, b);
    AlignmentTable2 table = AlignmentTableCreator.createAlignmentTable(set);
    String expected = "A: the|black|and|white|cat\n";
    expected += "B: the|black|and|white|cat\n";
    assertEquals(expected, table.toString());
  }
}
