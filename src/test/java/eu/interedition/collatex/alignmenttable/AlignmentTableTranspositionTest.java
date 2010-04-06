package eu.interedition.collatex.alignmenttable;

import org.junit.Before;

import eu.interedition.collatex.input.builders.WitnessBuilder;

public class AlignmentTableTranspositionTest {

  private WitnessBuilder builder;

  @Before
  public void setup() {
    builder = new WitnessBuilder();
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
