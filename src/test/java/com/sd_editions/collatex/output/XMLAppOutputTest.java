package com.sd_editions.collatex.output;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.sd_editions.collatex.permutations.CollateCore;
import com.sd_editions.collatex.permutations.MatchUnmatch;

public class XMLAppOutputTest {

  /**
   * The first example from #6 (http://arts-itsee.bham.ac.uk/trac/interedition/ticket/6) (without witness C for now)
   */
  @Test
  public void testSimpleSubstitutionOutput() {
    CollateCore collateCore = new CollateCore("the black cat and the black mat", "the black dog and the black mat");
    List<MatchUnmatch> matchUnmatchList = collateCore.doCompareWitnesses(collateCore.getWitness(1), collateCore.getWitness(2));
    // FIXME find out which is the best permutation, not just take the first one 
    AppAlignmentTable alignmentTable = new AppAlignmentTable(matchUnmatchList.get(0));
    String xml = alignmentTable.toXML();
    assertEquals("<collation>the black <app><rdg wit=\"#A\">cat</rdg><rdg wit=\"#B\">dog</rdg></app> and the black mat</collation>", xml);
  }
}
