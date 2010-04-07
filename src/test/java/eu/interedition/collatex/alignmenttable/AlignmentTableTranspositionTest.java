package eu.interedition.collatex.alignmenttable;

import org.junit.Before;

import eu.interedition.collatex.input.builders.WitnessBuilder;

public class AlignmentTableTranspositionTest {

  private WitnessBuilder builder;

  @Before
  public void setup() {
    builder = new WitnessBuilder();
  }

  // HERE THERE IS NO TRANSPOSITION!
  //  @Test
  //  public void testGarbage() {
  //    final Witness a = builder.build("a b");
  //    final Witness b = builder.build("c a");
  //  }

}
