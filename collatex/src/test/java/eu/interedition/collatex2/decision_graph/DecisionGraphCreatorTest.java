package eu.interedition.collatex2.decision_graph;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;

import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.decision_graph.DGEdge;
import eu.interedition.collatex2.implementation.decision_graph.DGVertex;
import eu.interedition.collatex2.implementation.decision_graph.DecisionGraph;
import eu.interedition.collatex2.implementation.decision_graph.DecisionGraphCreator;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

public class DecisionGraphCreatorTest {

  // All the witness are equal
  // There are choices to be made however, since there is duplication of tokens
  @Test
  public void testDGAlignmentEverythingEqual() {
    CollateXEngine engine = new CollateXEngine();
    IWitness a = engine.createWitness("a", "The red cat and the black cat");
    IWitness b = engine.createWitness("b", "The red cat and the black cat");
    IVariantGraph vGraph = engine.graph(a);
    DecisionGraph decisionGraph = DecisionGraphCreator.buildDecisionGraph(vGraph, b);
    assertEquals(13, decisionGraph.vertexSet().size());

    // fetch vertices
    Iterator<DGVertex> topologicalOrder = decisionGraph.iterator();
    DGVertex start = topologicalOrder.next();
    DGVertex the1 = topologicalOrder.next();
    DGVertex the2 = topologicalOrder.next();
    DGVertex red = topologicalOrder.next();
    DGVertex cat1 = topologicalOrder.next();
    DGVertex cat2 = topologicalOrder.next();
    DGVertex and = topologicalOrder.next();
    DGVertex the3 = topologicalOrder.next();
    DGVertex the4 = topologicalOrder.next();
    DGVertex black = topologicalOrder.next();
    DGVertex cat3 = topologicalOrder.next();
    DGVertex cat4 = topologicalOrder.next();
    DGVertex stop = topologicalOrder.next();
    
    // fetch edges
    DGEdge edge1 = decisionGraph.edge(start, the1);
    DGEdge edge2 = decisionGraph.edge(start, the2);
    DGEdge edge3 = decisionGraph.edge(the1, red);
    DGEdge edge4 = decisionGraph.edge(the2, red);
    DGEdge edge5 = decisionGraph.edge(red, cat1);
    DGEdge edge6 = decisionGraph.edge(red, cat2);
    DGEdge edge7 = decisionGraph.edge(cat1, and);
    DGEdge edge8 = decisionGraph.edge(cat2, and);
    DGEdge edge9 = decisionGraph.edge(and, the3);
    DGEdge edge10 = decisionGraph.edge(and, the4);
    DGEdge edge11 = decisionGraph.edge(the3, black);
    DGEdge edge12 = decisionGraph.edge(the4, black);
    DGEdge edge13 = decisionGraph.edge(black, cat3);
    DGEdge edge14 = decisionGraph.edge(black, cat4);
    DGEdge edge15 = decisionGraph.edge(cat3, stop);
    DGEdge edge16 = decisionGraph.edge(cat4, stop);
    
    // assert weight edges
    assertEquals(new Integer(0), edge1.getWeight());
    assertEquals(new Integer(1), edge2.getWeight());
    assertEquals(new Integer(0), edge3.getWeight());
    assertEquals(new Integer(1), edge4.getWeight());
    assertEquals(new Integer(0), edge5.getWeight());
    assertEquals(new Integer(1), edge6.getWeight());
    assertEquals(new Integer(0), edge7.getWeight());
    assertEquals(new Integer(1), edge8.getWeight());
    assertEquals(new Integer(1), edge9.getWeight());
    assertEquals(new Integer(0), edge10.getWeight());
    assertEquals(new Integer(1), edge11.getWeight());
    assertEquals(new Integer(0), edge12.getWeight());
    assertEquals(new Integer(1), edge13.getWeight());
    assertEquals(new Integer(0), edge14.getWeight());
    assertEquals(new Integer(1), edge15.getWeight());
    assertEquals(new Integer(0), edge16.getWeight());
  }


}
