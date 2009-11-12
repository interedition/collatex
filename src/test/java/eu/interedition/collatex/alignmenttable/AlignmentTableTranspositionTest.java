package eu.interedition.collatex.alignmenttable;

import static org.junit.Assert.assertEquals;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.interedition.collatex.alignment.Alignment;
import eu.interedition.collatex.alignment.functions.Matcher;
import eu.interedition.collatex.alignment.multiple_witness.AlignmentTable2;
import eu.interedition.collatex.alignment.multiple_witness.AlignmentTableCreator;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.WitnessSet;
import eu.interedition.collatex.input.Word;
import eu.interedition.collatex.input.builders.WitnessBuilder;

public class AlignmentTableTranspositionTest {

  private WitnessBuilder builder;

  @Before
  public void setup() {
    builder = new WitnessBuilder();
  }

  @Test
  public void testNoTransposition() {
    final Witness a = builder.build("no transposition");
    final Witness b = builder.build("no transposition");
    final Alignment<Word> al = Matcher.align(a, b);
    Assert.assertTrue(al.getTranpositions().isEmpty());
  }

  @Test
  public void testDoubleTransposition() {
    final Witness a = builder.build("a b");
    final Witness b = builder.build("b a");
    final Alignment<Word> al = Matcher.align(a, b);
    Assert.assertEquals(2, al.getTranpositions().size());
    // 1: a -> b
    // 2: b -> a
  }

  @Test
  public void testDoubleTransposition2() {
    final Witness a = builder.build("A", "a b");
    final Witness b = builder.build("B", "b a");
    final WitnessSet set = new WitnessSet(a, b);
    final AlignmentTable2 alignmentTable = AlignmentTableCreator.createAlignmentTable(set);
    final String expected = "A:  |a|b\n" + "B: b|a| \n";
    final String actual = alignmentTable.toString();
    Assert.assertEquals(expected, actual);
  }

  //TODO: make test work to see what happens if a does not stand at the end!
  @Ignore
  @Test
  public void testDoubleTransposition3() {
    final Witness a = builder.build("A", "a b c");
    final Witness b = builder.build("B", "b a c");
    final WitnessSet set = new WitnessSet(a, b);
    final AlignmentTable2 alignmentTable = AlignmentTableCreator.createAlignmentTable(set);
    final String expected = "A: a|b| |c\n" + "B:  |b|a |c\n";
    final String actual = alignmentTable.toString();
    Assert.assertEquals(expected, actual);
  }

  // TODO: change expectations and make this work!
  @Ignore
  @Test
  public void testTranspositionsAreNotStoredInAlignmentTable() {
    final Witness a = builder.build("A", "the black and white cat");
    final Witness b = builder.build("B", "the white and black cat");
    final WitnessSet set = new WitnessSet(a, b);
    final AlignmentTable2 table = AlignmentTableCreator.createAlignmentTable(set);
    String expected = "A: the|black|and|white|cat\n";
    expected += "B: the|black|and|white|cat\n";
    assertEquals(expected, table.toString());
  }

  // THIS SHOULD NOT REALLY MATTER BECAUSE OF THE SEQUENCE DETECTION!
  //  @Test
  //  public void testMultipleTransposition() {
  //    final Witness a = builder.build("a b c");
  //    final Witness b = builder.build("b c a");
  //    final Alignment<Word> al = Matcher.align(a, b);
  //    Assert.assertTrue(al.getTranpositions().isEmpty());
  //
  //  }

  // HERE THERE IS NO TRANSPOSITION!
  //  @Test
  //  public void testGarbage() {
  //    final Witness a = builder.build("a b");
  //    final Witness b = builder.build("c a");
  //  }

}
