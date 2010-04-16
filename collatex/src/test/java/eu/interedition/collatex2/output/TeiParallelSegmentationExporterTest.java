package eu.interedition.collatex2.output;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IWitness;

public class TeiParallelSegmentationExporterTest {
  private static CollateXEngine engine;

  @BeforeClass
  public static void setUp() {
    engine = new CollateXEngine();
  }

  private String collateWitnessStrings(final String a, final String b, final String c) {
    final IWitness w1 = engine.createWitness("A", a);
    final IWitness w2 = engine.createWitness("B", b);
    final IWitness w3 = engine.createWitness("C", c);
    IAlignmentTable table = engine.align(w1, w2, w3);
    ParallelSegmentationApparatus apparatus = engine.createApparatus(table);
    TeiParallelSegmentationExporter teiExporter = new TeiParallelSegmentationExporter(apparatus);
    return teiExporter.toTeiXML();
  }

  private String collateTwoWitnessStrings(final String a, final String b) {
    final IWitness w1 = engine.createWitness("A", a);
    final IWitness w2 = engine.createWitness("B", b);
    IAlignmentTable table = engine.align(w1, w2);
    ParallelSegmentationApparatus apparatus = engine.createApparatus(table);
    TeiParallelSegmentationExporter teiExporter = new TeiParallelSegmentationExporter(apparatus);
    return teiExporter.toTeiXML();
  }

  /**
   * The first example from #6 (http://arts-itsee.bham.ac.uk/trac/interedition/ticket/6) (without witness C for now)
   */
  @Test
  public void testSimpleSubstitutionOutput() {
    final String xml = collateWitnessStrings("the black cat and the black mat", "the black dog and the black mat", "the black dog and the black mat");
    Assert.assertEquals("<collation><seg>the black <app><rdg wit=\"#A\">cat</rdg><rdg wit=\"#B #C\">dog</rdg></app> and the black mat</seg></collation>", xml);
  }

  /**
   * Second example from #6. Tests addition, deletion and multiple words in one variant 
   */
  @Test
  public void testSimpleAddDelOutput() {
    final String xml = collateWitnessStrings("the black cat on the white table", 
                                             "the black saw the black cat on the table", 
                                             "the black saw the black cat on the table");
    Assert.assertEquals(
        "<collation><seg><app><rdg wit=\"#A\"/><rdg wit=\"#B #C\">the black saw</rdg></app> the black cat on the <app><rdg wit=\"#A\">white</rdg><rdg wit=\"#B #C\"/></app> table</seg></collation>",
        xml);
  }

  @Test
  public void testMultiSubstitutionOutput() {
    final String xml = collateWitnessStrings("the black cat and the black mat", "the big white dog and the black mat", "the big white dog and the black mat");
    Assert.assertEquals("<collation><seg>the <app><rdg wit=\"#A\">black cat</rdg><rdg wit=\"#B #C\">big white dog</rdg></app> and the black mat</seg></collation>", xml);
  }

  // Additional unit tests (not present in ticket #6)
  @Test
  public void testAllWitnessesEqual() {
    final String xml = collateWitnessStrings("the black cat", "the black cat", "the black cat");
    final String expected = "<collation><seg>the black cat</seg></collation>";
    Assert.assertEquals(expected, xml);
  }

  // Note: There are some problems with whitespace here!
  @Test
  public void testAWordMissingAtTheEnd() {
    final String xml = collateWitnessStrings("the black cat", "the black cat", "the black");
    final String expected = "<collation><seg>the black <app><rdg wit=\"#A #B\">cat</rdg><rdg wit=\"#C\"/></app></seg></collation>";
    Assert.assertEquals(expected, xml);
  }

  // Note: There might be some problems with whitespace here!
  @Test
  public void testCrossVariation() {
    final String xml = collateWitnessStrings("the black cat", "the white and black cat", "the white cat");
    final String expected = "<collation><seg>the <app><rdg wit='#A'/><rdg wit='#B #C'>white</rdg></app> <app><rdg wit='#A #C'/><rdg wit='#B'>and</rdg></app> <app><rdg wit='#A #B'>black</rdg><rdg wit='#C'/></app> cat</seg></collation>"
        .replaceAll("\\'", "\\\"");
    Assert.assertEquals(expected, xml);
  }

  // Note: There might be some problems with whitespace here!
  @Test
  public void testAddition() {
    final String xml = collateTwoWitnessStrings("the black cat", "the white and black cat");
    final String expected = "<collation><seg>the <app><rdg wit=\"#A\"/><rdg wit=\"#B\">white and</rdg></app> black cat</seg></collation>";
    Assert.assertEquals(expected, xml);
  }

  // TODO: reenable test!
  //  @Test
  //  public void testNearMatches() {
  //    Witness w1 = builder.build("A", "the black cat");
  //    Witness w2 = builder.build("B", "the blak cat");
  //    Witness w3 = builder.build("C", "the black cat");
  //    WitnessSet set = new WitnessSet(w1, w2, w3);
  //    AlignmentTable2 table = set.createAlignmentTable();
  //    String expected = "<collation>the black cat</collation>";
  //    Assert.assertEquals(expected, table.toXML());
  //  }

}
