package eu.interedition.collatex2.experimental.table;
import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex2.experimental.graph.IModifiableVariantGraph;
import eu.interedition.collatex2.experimental.graph.ModifiableVariantGraph;
import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IWitness;


public class DirectedAcyclicGraphBasedAlignmentTest {
    private static CollateXEngine engine;

    @BeforeClass
    public static void setup() {
      engine = new CollateXEngine();
    }

    @Test
    public void testEmptyGraph() {
      IModifiableVariantGraph graph = ModifiableVariantGraph.create();
      IAlignmentTable table = new DirectedAcyclicGraphBasedAlignmentTable(graph);
      assertEquals(0, table.getRows().size());
    }

    @Test
    public void testFirstWitness() {
      IWitness a = engine.createWitness("A", "the first witness");
      IModifiableVariantGraph graph = ModifiableVariantGraph.create();
      graph.addWitness(a);
      IAlignmentTable table = new DirectedAcyclicGraphBasedAlignmentTable(graph);
      assertEquals("A: |the|first|witness|", table.getRow(a).rowToString());
      assertEquals(1, table.getRows().size());
    }
    
    @Test
    public void testMultipleEqualWitnesses() {
      IWitness a = engine.createWitness("A", "everything matches");
      IWitness b = engine.createWitness("B", "everything matches");
      IWitness c = engine.createWitness("C", "everything matches");
      IModifiableVariantGraph graph = ModifiableVariantGraph.create();
         graph.addWitness(a);
      graph.addWitness(b);
      graph.addWitness(c);
      IAlignmentTable table = new DirectedAcyclicGraphBasedAlignmentTable(graph);
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
      IModifiableVariantGraph graph = ModifiableVariantGraph.create();
      graph.addWitness(w1);
      graph.addWitness(w2);
      graph.addWitness(w3);
      IAlignmentTable table = new DirectedAcyclicGraphBasedAlignmentTable(graph);
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
      IModifiableVariantGraph graph = ModifiableVariantGraph.create();
      graph.addWitness(w1);
      graph.addWitness(w2);
      graph.addWitness(w3);
      graph.addWitness(w4);
      graph.addWitness(w5);
      IAlignmentTable table = new DirectedAcyclicGraphBasedAlignmentTable(graph);
      assertEquals("A: |the|black|cat|", table.getRow(w1).rowToString());
      assertEquals("B: |the|white|cat|", table.getRow(w2).rowToString());
      assertEquals("C: |the|green|cat|", table.getRow(w3).rowToString());
      assertEquals("D: |the|red|cat|", table.getRow(w4).rowToString());
      assertEquals("E: |the|yellow|cat|", table.getRow(w5).rowToString());
      assertEquals(5, table.getRows().size());
    }


}
