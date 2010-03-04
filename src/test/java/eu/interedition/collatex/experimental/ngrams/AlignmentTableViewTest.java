package eu.interedition.collatex.experimental.ngrams;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import eu.interedition.collatex.experimental.ngrams.data.Witness;
import eu.interedition.collatex.experimental.ngrams.table.AlignmentTable;

public class AlignmentTableViewTest {
  @Test
  public void testSimple() {
    final String w1 = "a b";
    final String w2 = "a b";
    final String expected = "<xml>a b</xml>";
    assertEquals(expected, getGoing(w1, w2));
  }

  @Test
  public void testAddition() {
    final String w1 = "a b";
    final String w2 = "a c b";
    final String expected = "<xml>a <app>c</app> b</xml>";
    assertEquals(expected, getGoing(w1, w2));
  }

  @Test
  public void testOmmission() {
    final String w1 = "a c b";
    final String w2 = "a b";
    final String expected = "<xml>a <app>c</app> b</xml>";
    assertEquals(expected, getGoing(w1, w2));
  }

  @Test
  public void testReplacement() {
    final String w1 = "a c b";
    final String w2 = "a d b";
    final String expected = "<xml>a <app><lemma>c</lemma><reading>d</reading></app> b</xml>";
    assertEquals(expected, getGoing(w1, w2));
  }

  public String getGoing(final String w1, final String w2) {
    // TODO: make a constructor for Witness, Witness
    //    Witness base = new Witness(w1);
    //    Witness witness = new Witness(w2);
    //WitnessBuilder builder = new WitnessBuilder();
    //CollateCore core = new CollateCore(builder.build(w1), builder.build(w2));
    //Modifications modifications = core.compareWitness(1, 2);
    final Witness a = new Witness("A", w1);
    final Witness b = new Witness("B", w2);
    final Alignment alignment = Alignment.create(a, b);
    final AlignmentTable table = new AlignmentTable(alignment);
    final String xml = table.toXML();
    return xml;
  }
}
