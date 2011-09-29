package eu.interedition.collatex2.implementation.edit_graph;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Iterator;

import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.containers.witness.FakeWitness;
import eu.interedition.collatex2.implementation.matching.VariantGraphMatcher;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

public class EditGraphCreatorTest {

  @Test
  public void testExample1Mockito() {
//    CollateXEngine engine = new CollateXEngine();
//    IWitness a = engine.createWitness("a", "The red cat and the black cat");
//    IWitness b = engine.createWitness("b", "The red cat and the black cat");
//    IVariantGraph vGraph = engine.graph(a);
//    VariantGraphMatcher matcher = new VariantGraphMatcher();
    
    //setup witnesses
    FakeWitness base = new FakeWitness();
    INormalizedToken bThe = base.add("The");
    INormalizedToken bRed = base.add("red");
    INormalizedToken bCat = base.add("cat");
    INormalizedToken bAnd = base.add("and");
    INormalizedToken bThe2 = base.add("the");
    INormalizedToken bBlack = base.add("black");
    INormalizedToken bCat2 = base.add("cat");
    
    FakeWitness witness = new FakeWitness();
    INormalizedToken wThe = witness.add("The");
    INormalizedToken wRed = witness.add("red");
    INormalizedToken wCat = witness.add("cat");
    INormalizedToken wAnd = witness.add("and");
    INormalizedToken wThe2 = witness.add("the");
    INormalizedToken wBlack = witness.add("black");
    INormalizedToken wCat2 = witness.add("cat");
 
    //setup vertices
    EditGraphVertex startVertex = new EditGraphVertex(null, null); // vGraph.getStartVertex());
    EditGraphVertex vertex1 = new EditGraphVertex(wThe, bThe);
    EditGraphVertex vertex2 = new EditGraphVertex(wThe, bThe2);
    EditGraphVertex vertex3 = new EditGraphVertex(wRed, bRed);
    EditGraphVertex vertex4 = new EditGraphVertex(wCat, bCat);
    EditGraphVertex vertex5 = new EditGraphVertex(wCat, bCat2);
    EditGraphVertex vertex6 = new EditGraphVertex(wAnd, bAnd);
    EditGraphVertex vertex7 = new EditGraphVertex(wThe2, bThe);
    EditGraphVertex vertex8 = new EditGraphVertex(wThe2, bThe2);
    EditGraphVertex vertex9 = new EditGraphVertex(wBlack, bBlack);
    EditGraphVertex vertex10 = new EditGraphVertex(wCat2, bCat);
    EditGraphVertex vertex11 = new EditGraphVertex(wCat2, bCat2);
    
    
    //mock
    EditGraph editGraph = mock(EditGraph.class);
    when(editGraph.getStartVertex()).thenReturn(startVertex);
    
    //run
    EditGraphCreator creator = new EditGraphCreator(editGraph, null, null, null); // matcher, vGraph, b);
    creator.buildEditGraph(base, witness);
    
    //verify vertices
    verify(editGraph).getStartVertex();
    verify(editGraph).add(vertex1);
    verify(editGraph).add(vertex2);
    verify(editGraph).add(vertex3);
    verify(editGraph).add(vertex4);
    verify(editGraph).add(vertex5);
    verify(editGraph).add(vertex6);
    verify(editGraph).add(vertex7); // transposition!
    verify(editGraph).add(vertex8);
    verify(editGraph).add(vertex9);
    verify(editGraph).add(vertex10); // transposition!
    verify(editGraph).add(vertex11);
    
    //verify edges
    //TODO: add checks for edges!
    //verify(editGraph).add(new EditGraphEdge(startVertex, vertex1, 0));
    
    verifyNoMoreInteractions(editGraph);
  }
  
  
  
  
  
  
  
  
  
  
  //TODO: add a test where there is sometimes no match for a given token
  
  // All the witness are equal
  // There are choices to be made however, since there is duplication of tokens
  @Test
  public void testBuildingEditGraphEverythingEqual() {
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
  public void testEditGraphOmission() {
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
