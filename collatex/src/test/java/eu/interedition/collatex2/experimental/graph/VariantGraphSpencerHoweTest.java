package eu.interedition.collatex2.experimental.graph;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IWitness;

public class VariantGraphSpencerHoweTest {
  private static CollateXEngine engine;

  @BeforeClass
  public static void setup() {
    engine = new CollateXEngine();
  }

  //Note: this only tests the Graph, not the table!
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
  
  //maybe move test later
  @Test
  public void testEmptyGraph() {
    VariantGraph graph = VariantGraph.create();
    AlignmentTableCreator creator = new AlignmentTableCreator(graph);
    IAlignmentTable table = creator.getAlignmentTable();
    assertEquals(0, table.getRows().size());
  }
  
//maybe move test later
@Test
public void testFirstWitness() {
  IWitness w1 = engine.createWitness("A", "the first witness");
  VariantGraph graph = VariantGraph.create();
  graph.addWitness(w1);
  AlignmentTableCreator creator = new AlignmentTableCreator(graph);
  IAlignmentTable table = creator.getAlignmentTable();
  assertEquals(1, table.getRows().size());
}


//  //maybe move test later
//  @Test
//  public void testSimpleVariantGraphToAlignmentTable() {
//    IWitness w1 = engine.createWitness("A", "everything matches");
//    IWitness w2 = engine.createWitness("B", "everything matches");
//    IWitness w3 = engine.createWitness("C", "everything matches");
//    VariantGraph graph = VariantGraph.create();
//    graph.addWitness(w1);
//    graph.addWitness(w2);
//    graph.addWitness(w3);
//    AlignmentTableCreator creator = new AlignmentTableCreator(graph);
//    IAlignmentTable table = creator.getAlignmentTable();
//    assertEquals(0, table.getRows().size());
//  }

}
