package eu.interedition.collatex.alignmenttable;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import eu.interedition.collatex.alignment.Alignment;
import eu.interedition.collatex.alignment.functions.Matcher;
import eu.interedition.collatex.input.Witness;
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
