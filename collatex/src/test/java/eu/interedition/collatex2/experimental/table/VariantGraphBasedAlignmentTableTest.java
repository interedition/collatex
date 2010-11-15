package eu.interedition.collatex2.experimental.table;
import static org.junit.Assert.assertEquals;
import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.containers.graph.VariantGraph2Creator;
import eu.interedition.collatex2.implementation.output.table.VariantGraphBasedAlignmentTable;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;


public class VariantGraphBasedAlignmentTableTest {
    private static CollateXEngine engine;

    @BeforeClass
    public static void setup() {
      engine = new CollateXEngine();
    }

    @Test
    public void testFirstWitness() {
      IWitness a = engine.createWitness("A", "the first witness");
      IVariantGraph graph = VariantGraph2Creator.create(a);
      IAlignmentTable table = new VariantGraphBasedAlignmentTable(graph);
      assertEquals("A: |the|first|witness|", table.getRow(a).rowToString());
      assertEquals(1, table.getRows().size());
    }
    
    @Test
    public void testMultipleEqualWitnesses() {
      IWitness a = engine.createWitness("A", "everything matches");
      IWitness b = engine.createWitness("B", "everything matches");
      IWitness c = engine.createWitness("C", "everything matches");
      IVariantGraph graph = VariantGraph2Creator.create(a, b, c);
      IAlignmentTable table = new VariantGraphBasedAlignmentTable(graph);
      assertEquals("A: |everything|matches|", table.getRow(a).rowToString());
      assertEquals("B: |everything|matches|", table.getRow(b).rowToString());
      assertEquals("C: |everything|matches|", table.getRow(c).rowToString());
      assertEquals(3, table.getRows().size());
    }

    @Test
    public void testSimpleSpencerHowe() {
      IWitness w1 = engine.createWitness("A", "a");
      IWitness w2 = engine.createWitness("B", "b");
      IWitness w3 = engine.createWitness("C", "a b");
      IVariantGraph graph = VariantGraph2Creator.create(w1, w2, w3);
      IAlignmentTable table = new VariantGraphBasedAlignmentTable(graph);
      assertEquals("A: |a| |", table.getRow(w1).rowToString());
      assertEquals("B: | |b|", table.getRow(w2).rowToString());
      assertEquals("C: |a|b|", table.getRow(w3).rowToString());
      assertEquals(3, table.getRows().size());
    }
    
    @Test
    public void testVariant() {
      final IWitness w1 = engine.createWitness("A", "the black cat");
      final IWitness w2 = engine.createWitness("B", "the white cat");
      final IWitness w3 = engine.createWitness("C", "the green cat");
      final IWitness w4 = engine.createWitness("D", "the red cat");
      final IWitness w5 = engine.createWitness("E", "the yellow cat");
      IVariantGraph graph = VariantGraph2Creator.create(w1, w2, w3, w4, w5);
      IAlignmentTable table = new VariantGraphBasedAlignmentTable(graph);
      assertEquals("A: |the|black|cat|", table.getRow(w1).rowToString());
      assertEquals("B: |the|white|cat|", table.getRow(w2).rowToString());
      assertEquals("C: |the|green|cat|", table.getRow(w3).rowToString());
      assertEquals("D: |the|red|cat|", table.getRow(w4).rowToString());
      assertEquals("E: |the|yellow|cat|", table.getRow(w5).rowToString());
      assertEquals(5, table.getRows().size());
    }

    //NOTE: test taken from AlignmentTableTranspositionTest
    //TODO: rewrite test to work with the new API
    @Ignore
    @Test
    public void testDoubleTransposition2() {
      final IWitness a = engine.createWitness("A", "a b");
      final IWitness b = engine.createWitness("B", "b a");
      final IAlignmentTable alignmentTable = engine.align(a, b);
      final String expected = "A:  |a|b\n" + "B: b|a| \n";
      final String actual = alignmentTable.toString();
      Assert.assertEquals(expected, actual);
    }

}
