package eu.interedition.collatex2.implementation.edit_graph;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import java.util.Iterator;

import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.containers.witness.FakeWitness;
import eu.interedition.collatex2.implementation.matching.VariantGraphMatcher;
import eu.interedition.collatex2.implementation.vg_alignment.EndToken;
import eu.interedition.collatex2.implementation.vg_alignment.StartToken;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

//TODO: add a test where there is sometimes no match for a given token
public class EditGraphCreatorTest {

  // All the witness are equal
  // There are choices to be made however, since there is duplication of tokens
  // "The red cat and the black cat"
  // "The red cat and the black cat"
  @Test
  public void testBuildingEditGraphEverythingEqual() {
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
    EditGraphVertex startVertex = new EditGraphVertex(null, new StartToken()); // vGraph.getStartVertex());
    EditGraphVertex endVertex = new EditGraphVertex(null, new EndToken(base.size()+1));
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
    when(editGraph.getEndVertex()).thenReturn(endVertex);
    
    //run
    EditGraphCreator creator = new EditGraphCreator(editGraph, null, null, null); // matcher, vGraph, b);
    creator.buildEditGraph(base, witness);
    
    //verify vertices
    verify(editGraph).getStartVertex();
    verify(editGraph).getEndVertex();
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
    verify(editGraph).add(new EditGraphEdge(startVertex, vertex1, 0));
    verify(editGraph).add(new EditGraphEdge(startVertex, vertex2, 1));
    verify(editGraph).add(new EditGraphEdge(vertex1, vertex3, 0));
    verify(editGraph).add(new EditGraphEdge(vertex2, vertex3, 1)); // Transposition!
    verify(editGraph).add(new EditGraphEdge(vertex3, vertex4, 0));
    verify(editGraph).add(new EditGraphEdge(vertex3, vertex5, 1));
    verify(editGraph).add(new EditGraphEdge(vertex4, vertex6, 0));
    verify(editGraph).add(new EditGraphEdge(vertex5, vertex6, 1)); // Transposition!
    verify(editGraph).add(new EditGraphEdge(vertex6, vertex7, 1)); // Transposition!
    verify(editGraph).add(new EditGraphEdge(vertex6, vertex8, 0));
    verify(editGraph).add(new EditGraphEdge(vertex7, vertex9, 1)); // Transposition!
    verify(editGraph).add(new EditGraphEdge(vertex8, vertex9, 0));
    verify(editGraph).add(new EditGraphEdge(vertex9, vertex10, 1)); // Transposition!
    verify(editGraph).add(new EditGraphEdge(vertex9, vertex11, 0));
    verify(editGraph).add(new EditGraphEdge(vertex10, endVertex, 1)); // skip
    verify(editGraph).add(new EditGraphEdge(vertex11, endVertex, 0));
    
    verifyNoMoreInteractions(editGraph);
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
