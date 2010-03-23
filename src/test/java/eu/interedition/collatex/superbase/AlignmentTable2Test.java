package eu.interedition.collatex.superbase;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import eu.interedition.collatex.alignment.multiple_witness.AlignmentTable2;
import eu.interedition.collatex.alignment.multiple_witness.AlignmentTableCreator;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.WitnessSet;
import eu.interedition.collatex.input.builders.WitnessBuilder;

public class AlignmentTable2Test {
  @Test
  public void testStringOutputOneWitness() {
    final WitnessBuilder builder = new WitnessBuilder();
    final Witness a = builder.build("A", "the black cat");
    final WitnessSet set = new WitnessSet(a);
    final AlignmentTable2 table = AlignmentTableCreator.createAlignmentTable(set);
    final String expected = "A: the|black|cat\n";
    assertEquals(expected, table.toString());
  }

  @Test
  public void testStringOutputTwoWitnesses() {
    final WitnessBuilder builder = new WitnessBuilder();
    final Witness a = builder.build("A", "the black cat");
    final Witness b = builder.build("B", "the black cat");
    final WitnessSet set = new WitnessSet(a, b);
    final AlignmentTable2 table = AlignmentTableCreator.createAlignmentTable(set);
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
    final WitnessBuilder builder = new WitnessBuilder();
    final Witness a = builder.build("A", "the black cat");
    final Witness b = builder.build("B", "the");
    final WitnessSet set = new WitnessSet(a, b);
    final AlignmentTable2 table = AlignmentTableCreator.createAlignmentTable(set);
    // TODO: add match test can be moved to column class?
    //    Column column = table.getColumns().get(0);
    // TODO: word contains id also, which refers to Witness
    //table.addMatch(w2, w2.getWordOnPosition(1), column);
    String expected = "A: the|black|cat\n";
    expected += "B: the| | \n";
    assertEquals(expected, table.toString());
  }

}
