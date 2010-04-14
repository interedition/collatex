package eu.interedition.collatex2.alignmenttable;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IWitness;

/**
 * Testing the dependence of the algorithm on the order of witnesses.
 * 
 * <p>
 * See Matthew Spencer and Christopher J. Howe
 * "Collating Texts Using Progressive Multiple Alignment".
 * </p>
 * 
 * @author Gregor Middell
 * 
 */
public class SpencerHoweTest {
  private static Logger logger = LoggerFactory.getLogger(SpencerHoweTest.class);

  private CollateXEngine engine = new CollateXEngine();

  @Test
  @Ignore
  public void testEverythingMatches() {
    final IWitness w1 = engine.createWitness("V", "a b c d e f ");
    final IWitness w2 = engine.createWitness("W", "x y z d e");
    final IWitness w3 = engine.createWitness("X", "a b x y z");
    final IAlignmentTable table = engine.align(w1, w2, w3);
    logger.debug(table.toString());
    String expected = "V: a|b|-|c|-|d|e|f\n";
    expected += "W: -|-|x|y|z|d|e|-\n";
    expected += "X: a|b|x|y|z|-|-|-\n";
    Assert.assertEquals(expected, table.toString());
  }
}
