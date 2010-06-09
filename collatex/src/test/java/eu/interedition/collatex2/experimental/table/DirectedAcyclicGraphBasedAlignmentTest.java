package eu.interedition.collatex2.experimental.table;
import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex2.experimental.graph.VariantGraph;
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
      VariantGraph graph = VariantGraph.create();
      IAlignmentTable table = new DirectedAcyclicGraphBasedAlignmentTable(graph);
      assertEquals(0, table.getRows().size());
    }

    @Test
    public void testFirstWitness() {
      IWitness a = engine.createWitness("A", "the first witness");
      VariantGraph graph = VariantGraph.create();
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
      VariantGraph graph = VariantGraph.create();
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
      VariantGraph graph = VariantGraph.create();
      graph.addWitness(w1);
      graph.addWitness(w2);
      graph.addWitness(w3);
      IAlignmentTable table = new DirectedAcyclicGraphBasedAlignmentTable(graph);
      assertEquals("A: |a| |", table.getRow(w1).rowToString());
      assertEquals("B: | |b|", table.getRow(w2).rowToString());
      assertEquals("C: |a|b|", table.getRow(w3).rowToString());
      assertEquals(3, table.getRows().size());
    }

}
