package eu.interedition.collatex2.alignmenttable;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.interedition.collatex2.experimental.output.table.VariantGraphBasedAlignmentTable;
import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.containers.graph.VariantGraph2Creator;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IVariantGraph;
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
  //@Ignore
  public void testATSpencerHowe() {
    final IWitness w1 = engine.createWitness("V", "a b c d e f ");
    final IWitness w2 = engine.createWitness("W", "x y z d e");
    final IWitness w3 = engine.createWitness("X", "a b x y z");
    IVariantGraph graph = VariantGraph2Creator.create(w1, w2, w3);
    IAlignmentTable table = new VariantGraphBasedAlignmentTable(graph);
    assertEquals("V: |a|b|c| | |d|e|f|", table.getRow(w1).rowToString());
    assertEquals("W: | | |x|y|z|d|e| |", table.getRow(w2).rowToString());
    assertEquals("X: |a|b|x|y|z| | | |", table.getRow(w3).rowToString());
    assertEquals(3, table.getRows().size());
  }
}
