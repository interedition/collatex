package com.sd_editions.collatex.output;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.sd_editions.collatex.permutations.CollateCore;
import com.sd_editions.collatex.permutations.MatchNonMatch;
import com.sd_editions.collatex.permutations.WitnessBuilder;

public class XMLAppOutputTest {

  private static WitnessBuilder builder;

  @BeforeClass
  public static void setUp() {
    builder = new WitnessBuilder();
  }

  /**
   * The first example from #6 (http://arts-itsee.bham.ac.uk/trac/interedition/ticket/6) (without witness C for now)
   */
  @Test
  public void testSimpleSubstitutionOutput() {
    String xml = collateWitnessStrings("the black cat and the black mat", "the black dog and the black mat");
    assertEquals("<collation>the black <app><rdg wit=\"#A\">cat</rdg><rdg wit=\"#B\">dog</rdg></app> and the black mat</collation>", xml);
  }

  /**
   * Second example from #6. Tests addition, deletion and multiple words in one variant 
   */
  @Test
  public void testSimpleAddDelOutput() {
    String xml = collateWitnessStrings("the black cat on the white table", "the black saw the black cat on the table");
    assertEquals("<collation>the black <app><rdg wit=\"#A\"/><rdg wit=\"#B\">saw the black</rdg></app> cat on the <app><rdg wit=\"#A\">white</rdg><rdg wit=\"#B\"/></app> table</collation>", xml);
  }

  @Test
  public void testMultiSubstitutionOutput() {
    String xml = collateWitnessStrings("the black cat and the black mat", "the big white dog and the black mat");
    assertEquals("<collation>the <app><rdg wit=\"#A\">black cat</rdg><rdg wit=\"#B\">big white dog</rdg></app> and the black mat</collation>", xml);
  }

  private String collateWitnessStrings(String witnessA, String witnessB) {
    WitnessBuilder builder = new WitnessBuilder();
    CollateCore collateCore = new CollateCore(builder.build(witnessA), builder.build(witnessB)); // ignored actually.
    List<MatchNonMatch> matchNonMatchList = collateCore.doCompareWitnesses(builder.build("A", witnessA), builder.build("B", witnessB));

    collateCore.sortPermutationsByNonMatches(matchNonMatchList);

    for (MatchNonMatch matchNonMatch : matchNonMatchList) {
      System.out.println(new AppAlignmentTable(matchNonMatch).toXML());
    }

    AppAlignmentTable alignmentTable = new AppAlignmentTable(matchNonMatchList.get(0));
    String xml = alignmentTable.toXML();
    return xml;
  }

}
