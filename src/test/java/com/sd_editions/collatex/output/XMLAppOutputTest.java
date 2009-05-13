package com.sd_editions.collatex.output;

import org.junit.BeforeClass;

import com.sd_editions.collatex.permutations.WitnessBuilder;

public class XMLAppOutputTest {

  private static WitnessBuilder builder;

  @BeforeClass
  public static void setUp() {
    builder = new WitnessBuilder();
  }

  //  @Test
  //  public void testTwoWitnesses() {
  //    Witness w1 = builder.build("A", "the black cat");
  //    Witness w2 = builder.build("B", "the white and black cat");
  //    MagicClass2 magic = new MagicClass2(w1, w2);
  //    AlignmentTable2 table = magic.createAlignmentTable();
  //    String expected = "A: the| | |black|cat";
  //    expected += "B: the|white|and|black|cat";
  //    table.toString();
  //  }

  //  @Test
  //  public void testThreeWitnesses() {
  //    Witness w1 = builder.build("the black cat");
  //    Witness w2 = builder.build("the white and black cat");
  //    Witness w3 = builder.build("the white cat");
  //    MagicClass2 magic = new MagicClass2(w1, w2, w3);
  //    AlignmentTable2 table = magic.createAlignmentTable();
  //    String expected = "<xml>the <app><rdg wit='#B #C'>black </rdg><rdg wit='#A'></rdg></app><app><rdg wit='#B'>and </rdg><rdg wit='#A #c'></rdg></app><app><rdg wit='#A #B'>white </rdg><rdg wit='#C'></rdg></app>cat</xml>"
  //        .replaceAll("\\'", "\\\\");
  //    assertEquals(expected, table.toXML());
  //  }

  /**
   * The first example from #6 (http://arts-itsee.bham.ac.uk/trac/interedition/ticket/6) (without witness C for now)
   */
  //  @Test
  //  public void testSimpleSubstitutionOutput() {
  //    String xml = collateWitnessStrings("the black cat and the black mat", "the black dog and the black mat");
  //    assertEquals("<collation>the black <app><rdg wit=\"#A\">cat</rdg><rdg wit=\"#B\">dog</rdg></app> and the black mat</collation>", xml);
  //  }
  //
  //  /**
  //   * Second example from #6. Tests addition, deletion and multiple words in one variant 
  //   */
  //  @Test
  //  public void testSimpleAddDelOutput() {
  //    String xml = collateWitnessStrings("the black cat on the white table", "the black saw the black cat on the table");
  //    assertEquals("<collation>the black <app><rdg wit=\"#A\"/><rdg wit=\"#B\">saw the black</rdg></app> cat on the <app><rdg wit=\"#A\">white</rdg><rdg wit=\"#B\"/></app> table</collation>", xml);
  //  }
  //
  //  @Test
  //  public void testMultiSubstitutionOutput() {
  //    String xml = collateWitnessStrings("the black cat and the black mat", "the big white dog and the black mat");
  //    assertEquals("<collation>the <app><rdg wit=\"#A\">black cat</rdg><rdg wit=\"#B\">big white dog</rdg></app> and the black mat</collation>", xml);
  //  }
  //
  //  private String collateWitnessStrings(String witnessA, String witnessB) {
  //    WitnessBuilder builder = new WitnessBuilder();
  //    CollateCore collateCore = new CollateCore(builder.build(witnessA), builder.build(witnessB)); // ignored actually.
  //    List<MatchNonMatch> matchNonMatchList = collateCore.doCompareWitnesses(builder.build("A", witnessA), builder.build("B", witnessB));
  //
  //    collateCore.sortPermutationsByNonMatches(matchNonMatchList);
  //
  //    for (MatchNonMatch matchNonMatch : matchNonMatchList) {
  //      System.out.println(new AppAlignmentTable(matchNonMatch).toXML());
  //    }
  //
  //    AppAlignmentTable alignmentTable = new AppAlignmentTable(matchNonMatchList.get(0));
  //    String xml = alignmentTable.toXML();
  //    return xml;
  //  }
}
