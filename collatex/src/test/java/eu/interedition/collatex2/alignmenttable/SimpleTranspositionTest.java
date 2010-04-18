package eu.interedition.collatex2.alignmenttable;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IWitness;

public class SimpleTranspositionTest {
  private static Logger logger = LoggerFactory.getLogger(SpencerHoweTest.class);
  private CollateXEngine engine = new CollateXEngine();

  @Test
  @Ignore
  public void testSimpleTransposition() {
    final IWitness w1 = engine.createWitness("A", "A black cat in a white basket");
    final IWitness w2 = engine.createWitness("B", "A white cat in a black basket");
    final IAlignmentTable table = engine.align(w1, w2);
    logger.debug(table.toString());
    String expected = "A: a|black|cat|in|a|white|basket\n";
    expected += "B: a|white|cat|in|a|black|basket\n";
    Assert.assertEquals(expected, table.toString());
  }

}
