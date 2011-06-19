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
    assertEquals(5, decisionGraph.vertexSet().size());
    Iterator<DGVertex> topologicalOrder = decisionGraph.iterator();
    //TODO: move stop to the end!
    DGVertex start = topologicalOrder.next();
    // fetch vertices
    DGVertex stop = topologicalOrder.next();
    DGVertex the1 = topologicalOrder.next();
    DGVertex the2 = topologicalOrder.next();
    DGVertex red = topologicalOrder.next();
    // fetch edges
    DGEdge edge1 = decisionGraph.edge(start, the1);
    DGEdge edge2 = decisionGraph.edge(start, the2);
    DGEdge edge3 = decisionGraph.edge(the1, red);
    DGEdge edge4 = decisionGraph.edge(the2, red);
    // assert weight edges
    assertEquals(new Integer(0), edge1.getWeight());
    assertEquals(new Integer(1), edge2.getWeight());
    assertEquals(new Integer(0), edge3.getWeight());
    assertEquals(new Integer(1), edge4.getWeight());
  }


}
