package eu.interedition.collatex2.experimental.graph;
import static org.junit.Assert.assertEquals;

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

// TODO: restore later!    
//    @Test
//    public void testEmptyGraph() {
//      VariantGraph graph = VariantGraph.create();
//      VariantGraphBasedAlignmentTable table = new VariantGraphBasedAlignmentTable(graph);
//      assertEquals(0, table.getRows().size());
//    }

    @Test
    public void testFirstWitness() {
      IWitness a = engine.createWitness("A", "the first witness");
      VariantGraph graph = VariantGraph.create();
      graph.addWitness(a);
      IAlignmentTable table = new DirectedAcyclicGraphBasedAlignmentTable(graph);
      assertEquals("A: |the|first|witness|", rowToString(table.getRow(a)));
      assertEquals(1, table.getRows().size());
    }

}
