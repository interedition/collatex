package eu.interedition.collatex2.implementation.edit_graph;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import eu.interedition.collatex2.implementation.input.NormalizedToken;
import eu.interedition.collatex2.implementation.matching.EqualityTokenComparator;
import org.junit.Test;

import eu.interedition.collatex2.interfaces.INormalizedToken;

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
    EditGraphVertex startVertex = new EditGraphVertex(null, NormalizedToken.START); // vGraph.getStartVertex());
    EditGraphVertex endVertex = new EditGraphVertex(null, NormalizedToken.END);
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
    
    //run
    EditGraphCreator creator = new EditGraphCreator(editGraph);
    creator.buildEditGraph(base, witness, new EqualityTokenComparator());
    
    //verify vertices
    verify(editGraph).setStartVertex(startVertex);
    verify(editGraph).setEndVertex(endVertex);
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
    verify(editGraph).add(new EditGraphEdge(startVertex, vertex1, EditOperation.NO_GAP, 0));
    verify(editGraph).add(new EditGraphEdge(startVertex, vertex2, EditOperation.GAP, 1));
    verify(editGraph).add(new EditGraphEdge(vertex1, vertex3, EditOperation.NO_GAP, 0));
    verify(editGraph).add(new EditGraphEdge(vertex2, vertex3, EditOperation.GAP, 1)); // Transposition!
    verify(editGraph).add(new EditGraphEdge(vertex3, vertex4, EditOperation.NO_GAP, 0));
    verify(editGraph).add(new EditGraphEdge(vertex3, vertex5, EditOperation.GAP, 1));
    verify(editGraph).add(new EditGraphEdge(vertex4, vertex6, EditOperation.NO_GAP, 0));
    verify(editGraph).add(new EditGraphEdge(vertex5, vertex6, EditOperation.GAP, 1)); // Transposition!
    verify(editGraph).add(new EditGraphEdge(vertex6, vertex7, EditOperation.GAP, 1)); // Transposition!
    verify(editGraph).add(new EditGraphEdge(vertex6, vertex8, EditOperation.NO_GAP, 0));
    verify(editGraph).add(new EditGraphEdge(vertex7, vertex9, EditOperation.GAP, 1)); // Transposition!
    verify(editGraph).add(new EditGraphEdge(vertex8, vertex9, EditOperation.NO_GAP, 0));
    verify(editGraph).add(new EditGraphEdge(vertex9, vertex10, EditOperation.GAP, 1)); // Transposition!
    verify(editGraph).add(new EditGraphEdge(vertex9, vertex11, EditOperation.NO_GAP, 0));
    verify(editGraph).add(new EditGraphEdge(vertex10, endVertex, EditOperation.GAP, 1)); // skip
    verify(editGraph).add(new EditGraphEdge(vertex11, endVertex, EditOperation.NO_GAP, 0));
    
    verifyNoMoreInteractions(editGraph);
  }
  
  // The red cat and the black cat
  // the black cat
  @Test
  public void testBuildingEditGraphOmission() {
    //setup witnesses
    FakeWitness base = new FakeWitness();
    INormalizedToken bThe = base.add("The");
    /*INormalizedToken bRed =*/ base.add("red");
    INormalizedToken bCat = base.add("cat");
    /*INormalizedToken bAnd =*/ base.add("and");
    INormalizedToken bThe2 = base.add("the");
    INormalizedToken bBlack = base.add("black");
    INormalizedToken bCat2 = base.add("cat");
    
    FakeWitness witness = new FakeWitness();
    INormalizedToken wThe = witness.add("the");
    INormalizedToken wBlack = witness.add("black");
    INormalizedToken wCat = witness.add("cat");

    //setup vertices
    EditGraphVertex startVertex = new EditGraphVertex(null, NormalizedToken.START); // vGraph.getStartVertex());
    EditGraphVertex endVertex = new EditGraphVertex(null, NormalizedToken.END);
    EditGraphVertex vertex1 = new EditGraphVertex(wThe, bThe);
    EditGraphVertex vertex2 = new EditGraphVertex(wThe, bThe2);
    EditGraphVertex vertex3 = new EditGraphVertex(wBlack, bBlack);
    EditGraphVertex vertex4 = new EditGraphVertex(wCat, bCat);
    EditGraphVertex vertex5 = new EditGraphVertex(wCat, bCat2);
    
    //mock
    EditGraph editGraph = mock(EditGraph.class);
    
    //run
    EditGraphCreator creator = new EditGraphCreator(editGraph);
    creator.buildEditGraph(base, witness, new EqualityTokenComparator());
 
    //verify vertices
    verify(editGraph).setStartVertex(startVertex);
    verify(editGraph).setEndVertex(endVertex);
    verify(editGraph).add(vertex1);
    verify(editGraph).add(vertex2);
    verify(editGraph).add(vertex3);
    verify(editGraph).add(vertex4);
    verify(editGraph).add(vertex5);
 
    //verify edges
    verify(editGraph).add(new EditGraphEdge(startVertex, vertex1, EditOperation.NO_GAP, 0));
    verify(editGraph).add(new EditGraphEdge(startVertex, vertex2, EditOperation.GAP, 1));
    verify(editGraph).add(new EditGraphEdge(vertex1, vertex3, EditOperation.GAP, 1));
    verify(editGraph).add(new EditGraphEdge(vertex2, vertex3, EditOperation.NO_GAP, 0));
    verify(editGraph).add(new EditGraphEdge(vertex3, vertex4, EditOperation.GAP, 1));
    verify(editGraph).add(new EditGraphEdge(vertex3, vertex5, EditOperation.NO_GAP, 0));
    verify(editGraph).add(new EditGraphEdge(vertex4, endVertex, EditOperation.GAP, 1));
    verify(editGraph).add(new EditGraphEdge(vertex5, endVertex, EditOperation.NO_GAP, 0));
    
    verifyNoMoreInteractions(editGraph);
  }
}
