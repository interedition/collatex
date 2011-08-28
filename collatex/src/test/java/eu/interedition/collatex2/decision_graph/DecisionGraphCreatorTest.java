package eu.interedition.collatex2.decision_graph;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;

import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.decision_graph.DGEdge;
import eu.interedition.collatex2.implementation.decision_graph.DGVertex;
import eu.interedition.collatex2.implementation.decision_graph.DecisionGraph;
import eu.interedition.collatex2.implementation.decision_graph.DecisionGraphCreator;
import eu.interedition.collatex2.implementation.decision_graph.VariantGraphMatcher;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

public class DecisionGraphCreatorTest {

  //TODO: add a test where there is sometimes no match for a given token
  
  // All the witness are equal
  // There are choices to be made however, since there is duplication of tokens
  @Test
  public void testBuildingDGEverythingEqual() {
    CollateXEngine engine = new CollateXEngine();
    IWitness a = engine.createWitness("a", "The red cat and the black cat");
    IWitness b = engine.createWitness("b", "The red cat and the black cat");
    IVariantGraph vGraph = engine.graph(a);
    VariantGraphMatcher matcher = new VariantGraphMatcher();
    DecisionGraphCreator creator = new DecisionGraphCreator(matcher, vGraph, b);
    DecisionGraph decisionGraph = creator.buildDecisionGraph();
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

  @Test
  public void testDecisionGraphOmission() {
    CollateXEngine engine = new CollateXEngine();
    IWitness a = engine.createWitness("a", "The red cat and the black cat");
    IWitness b = engine.createWitness("b", "the black cat");
    IVariantGraph graph = engine.graph(a);
    VariantGraphMatcher matcher = new VariantGraphMatcher();
    DecisionGraphCreator creator = new DecisionGraphCreator(matcher, graph, b);
    DecisionGraph dGraph = creator.buildDecisionGraph();
    Iterator<DGVertex> topologicalOrder = dGraph.iterator();
    DGVertex v1 = topologicalOrder.next();
    DGVertex vThe1 = topologicalOrder.next();
    DGVertex vThe2 = topologicalOrder.next();
    DGEdge e1 = dGraph.edge(v1, vThe1);
    DGEdge e2 = dGraph.edge(v1, vThe2); 
    assertEquals(new Integer(0), e1.getWeight()); // 0 = no gap -> ENumeration?
    assertEquals(new Integer(1), e2.getWeight()); // 1 = gap
    DGVertex vB = topologicalOrder.next();
    DGEdge e3 = dGraph.edge(vThe1, vB); // , 1 
    DGEdge e4 = dGraph.edge(vThe2, vB); // , 0
    assertEquals(new Integer(1), e3.getWeight());
    assertEquals(new Integer(0), e4.getWeight());
    DGVertex vC1 = topologicalOrder.next();
    DGVertex vC2 = topologicalOrder.next();
    DGEdge e5 = dGraph.edge(vB, vC1); // , 1
    DGEdge e6 = dGraph.edge(vB, vC2); // , 0
    assertEquals(new Integer(1), e5.getWeight());
    assertEquals(new Integer(0), e6.getWeight());
    DGVertex end = topologicalOrder.next();
    DGEdge e7 = dGraph.edge(vC1, end); // , 1
    DGEdge e8 = dGraph.edge(vC2, end); // , 0
    assertEquals(new Integer(1), e7.getWeight());
    assertEquals(new Integer(0), e8.getWeight());
  }
}
