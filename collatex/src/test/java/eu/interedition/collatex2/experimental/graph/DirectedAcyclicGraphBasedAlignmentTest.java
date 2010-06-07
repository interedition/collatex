package eu.interedition.collatex2.experimental.graph;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.ICell;
import eu.interedition.collatex2.interfaces.IRow;
import eu.interedition.collatex2.interfaces.IWitness;


public class DirectedAcyclicGraphBasedAlignmentTest {
    private static CollateXEngine engine;

    @BeforeClass
    public static void setup() {
      engine = new CollateXEngine();
    }

    //TODO: add support for empty rows!
    private String rowToString(IRow row) {
      StringBuffer resultRow = new StringBuffer();
      resultRow.append(row.getSigil());
      resultRow.append(": ");
      for (ICell cell : row) {
        resultRow.append("|");
        resultRow.append(cell.getToken().getContent());
      }
      resultRow.append("|");
      return resultRow.toString();
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
      assertEquals("A: |the|first|witness|", rowToString(table.getRow(a)));
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
      assertEquals("A: |everything|matches|", rowToString(table.getRow(a)));
      assertEquals("B: |everything|matches|", rowToString(table.getRow(b)));
      assertEquals("C: |everything|matches|", rowToString(table.getRow(c)));
      assertEquals(3, table.getRows().size());
    }

    // TODO!!
    // Note: this only tests the Graph, not the table!
    @Test
    public void testSimpleSpencerHowe() {
      IWitness w1 = engine.createWitness("A", "a");
      IWitness w2 = engine.createWitness("B", "b");
      IWitness w3 = engine.createWitness("C", "a b");
      VariantGraph graph = VariantGraph.create();
      graph.addWitness(w1);
      graph.addWitness(w2);
      graph.addWitness(w3);
      assertEquals(3, graph.getNodes().size());
      final List<IVariantGraphArc> arcs = graph.getArcs();
      assertEquals(3, arcs.size());
      assertEquals("# -> a: A, C", arcs.get(0).toString());
      assertEquals("# -> b: B", arcs.get(1).toString());
      assertEquals("a -> b: C", arcs.get(2).toString());
    }

}
