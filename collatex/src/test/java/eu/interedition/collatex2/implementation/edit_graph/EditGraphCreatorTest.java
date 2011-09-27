package eu.interedition.collatex2.implementation.edit_graph;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;

import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.edit_graph.EditGraphEdge;
import eu.interedition.collatex2.implementation.edit_graph.EditGraphVertex;
import eu.interedition.collatex2.implementation.edit_graph.EditGraph;
import eu.interedition.collatex2.implementation.edit_graph.EditGraphCreator;
import eu.interedition.collatex2.implementation.matching.VariantGraphMatcher;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

public class EditGraphCreatorTest {

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
    EditGraphCreator creator = new EditGraphCreator(matcher, vGraph, b);
    EditGraph decisionGraph = creator.buildEditGraph();
    assertEquals(13, decisionGraph.vertexSet().size());

    // fetch vertices
    Iterator<EditGraphVertex> topologicalOrder = decisionGraph.iterator();
    EditGraphVertex start = topologicalOrder.next();
    EditGraphVertex the1 = topologicalOrder.next();
    EditGraphVertex the2 = topologicalOrder.next();
    EditGraphVertex red = topologicalOrder.next();
    EditGraphVertex cat1 = topologicalOrder.next();
    EditGraphVertex cat2 = topologicalOrder.next();
    EditGraphVertex and = topologicalOrder.next();
    EditGraphVertex the3 = topologicalOrder.next();
    EditGraphVertex the4 = topologicalOrder.next();
    EditGraphVertex black = topologicalOrder.next();
    EditGraphVertex cat3 = topologicalOrder.next();
    EditGraphVertex cat4 = topologicalOrder.next();
    EditGraphVertex stop = topologicalOrder.next();
    
    // fetch edges
    EditGraphEdge edge1 = decisionGraph.edge(start, the1);
    EditGraphEdge edge2 = decisionGraph.edge(start, the2);
    EditGraphEdge edge3 = decisionGraph.edge(the1, red);
    EditGraphEdge edge4 = decisionGraph.edge(the2, red);
    EditGraphEdge edge5 = decisionGraph.edge(red, cat1);
    EditGraphEdge edge6 = decisionGraph.edge(red, cat2);
    EditGraphEdge edge7 = decisionGraph.edge(cat1, and);
    EditGraphEdge edge8 = decisionGraph.edge(cat2, and);
    EditGraphEdge edge9 = decisionGraph.edge(and, the3);
    EditGraphEdge edge10 = decisionGraph.edge(and, the4);
    EditGraphEdge edge11 = decisionGraph.edge(the3, black);
    EditGraphEdge edge12 = decisionGraph.edge(the4, black);
    EditGraphEdge edge13 = decisionGraph.edge(black, cat3);
    EditGraphEdge edge14 = decisionGraph.edge(black, cat4);
    EditGraphEdge edge15 = decisionGraph.edge(cat3, stop);
    EditGraphEdge edge16 = decisionGraph.edge(cat4, stop);
    
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
    EditGraphCreator creator = new EditGraphCreator(matcher, graph, b);
    EditGraph dGraph = creator.buildEditGraph();
    Iterator<EditGraphVertex> topologicalOrder = dGraph.iterator();
    EditGraphVertex v1 = topologicalOrder.next();
    EditGraphVertex vThe1 = topologicalOrder.next();
    EditGraphVertex vThe2 = topologicalOrder.next();
    EditGraphEdge e1 = dGraph.edge(v1, vThe1);
    EditGraphEdge e2 = dGraph.edge(v1, vThe2); 
    assertEquals(new Integer(0), e1.getWeight()); // 0 = no gap -> ENumeration?
    assertEquals(new Integer(1), e2.getWeight()); // 1 = gap
    EditGraphVertex vB = topologicalOrder.next();
    EditGraphEdge e3 = dGraph.edge(vThe1, vB); // , 1 
    EditGraphEdge e4 = dGraph.edge(vThe2, vB); // , 0
    assertEquals(new Integer(1), e3.getWeight());
    assertEquals(new Integer(0), e4.getWeight());
    EditGraphVertex vC1 = topologicalOrder.next();
    EditGraphVertex vC2 = topologicalOrder.next();
    EditGraphEdge e5 = dGraph.edge(vB, vC1); // , 1
    EditGraphEdge e6 = dGraph.edge(vB, vC2); // , 0
    assertEquals(new Integer(1), e5.getWeight());
    assertEquals(new Integer(0), e6.getWeight());
    EditGraphVertex end = topologicalOrder.next();
    EditGraphEdge e7 = dGraph.edge(vC1, end); // , 1
    EditGraphEdge e8 = dGraph.edge(vC2, end); // , 0
    assertEquals(new Integer(1), e7.getWeight());
    assertEquals(new Integer(0), e8.getWeight());
  }
}
