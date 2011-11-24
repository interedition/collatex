package eu.interedition.collatex2.implementation.edit_graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import eu.interedition.collatex2.implementation.matching.EqualityTokenComparator;
import org.junit.Ignore;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IWitness;

public class EditGraphVisitorTest {

  public void assertVertices(EditGraph dGraph, String... normalized) {
    Iterator<EditGraphVertex> topologicIterator = dGraph.iterator();
    for (String expectedNormalized : normalized) {
      assertTrue("not enough vertices!", topologicIterator.hasNext());
      assertEquals(expectedNormalized, topologicIterator.next().getBaseToken().getNormalized());
    }
  }

  // All the witness are equal
  // There are choices to be made however, since there is duplication of tokens
  // Optimal alignment has no gaps
  @Test
  public void testGapsEverythingEqual() {
    CollateXEngine engine = new CollateXEngine();
    IWitness a = engine.createWitness("a", "The red cat and the black cat");
    IWitness b = engine.createWitness("b", "The red cat and the black cat");
    EditGraphCreator creator = new EditGraphCreator();
    EditGraph dGraph = creator.buildEditGraph(a, b, new EqualityTokenComparator());
    assertEquals(0, EditGraphVisitor.determineMinimumNumberOfGaps(dGraph));
  }

  // There is an omission
  // Optimal alignment has 1 gap
  // Note: there are two paths here that contain 1 gap
  @Test
  public void testGapsOmission() {
    CollateXEngine engine = new CollateXEngine();
    IWitness a = engine.createWitness("a", "The red cat and the black cat");
    IWitness b = engine.createWitness("b", "the black cat");
    EditGraphCreator creator = new EditGraphCreator();
    EditGraph dGraph = creator.buildEditGraph(a, b, new EqualityTokenComparator());
    assertEquals(1, EditGraphVisitor.determineMinimumNumberOfGaps(dGraph));
  }

  // first make a unit test which strips down the decision graph
  @Test
  public void testRemoveChoicesThatIntroduceGaps() {
    CollateXEngine engine = new CollateXEngine();
    IWitness a = engine.createWitness("a", "The red cat and the black cat");
    IWitness b = engine.createWitness("b", "the black cat");
    EditGraphCreator creator = new EditGraphCreator();
    EditGraph dGraph = creator.buildEditGraph(a, b, new EqualityTokenComparator());
    EditGraphVisitor visitor = new EditGraphVisitor(dGraph);
    EditGraph dGraph2 = visitor.removeChoicesThatIntroduceGaps();
    // I expect 6 vertices
    // start, 2 x the, black, cat en end
    assertVertices(dGraph2, "#", "the", "the", "black", "cat", "#");
  }
  
  
  //When there are multiple paths with the same minimum number of gaps
  //do a second pass that tries to find the longest common sequence
  @Test
  public void testTryToFindMinimumAmountOfSequences() {
    CollateXEngine engine = new CollateXEngine();
    IWitness a = engine.createWitness("a", "The red cat and the black cat");
    IWitness b = engine.createWitness("b", "the black cat");
    EditGraphCreator creator = new EditGraphCreator();
    EditGraph dGraph = creator.buildEditGraph(a, b, new EqualityTokenComparator());
    EditGraphVisitor visitor = new EditGraphVisitor(dGraph);
    EditGraph dGraph2 = visitor.removeChoicesThatIntroduceGaps();
    Map<EditGraphVertex, Integer> determineMinSequences = visitor.determineMinSequences(dGraph2);
    // asserts
    Iterator<EditGraphVertex> dgVerticesIterator = dGraph2.iterator();
    assertEquals(new Integer(1), determineMinSequences.get(dgVerticesIterator.next()));
    assertEquals(new Integer(2), determineMinSequences.get(dgVerticesIterator.next()));
    assertEquals(new Integer(1), determineMinSequences.get(dgVerticesIterator.next()));
    assertEquals(new Integer(1), determineMinSequences.get(dgVerticesIterator.next()));
    assertEquals(new Integer(1), determineMinSequences.get(dgVerticesIterator.next()));
    assertEquals(new Integer(1), determineMinSequences.get(dgVerticesIterator.next()));
  }

  @Test
  public void testShortestPathOneOmissionRepetition() {
    CollateXEngine engine = new CollateXEngine();
    IWitness a = engine.createWitness("a", "The red cat and the black cat");
    IWitness b = engine.createWitness("b", "the black cat");
    EditGraphCreator creator = new EditGraphCreator();
    EditGraph dGraph = creator.buildEditGraph(a, b, new EqualityTokenComparator());
    EditGraphVisitor visitor = new EditGraphVisitor(dGraph);
    List<EditGraphEdge> edges = visitor.getShortestPath();
    assertEquals(EditOperation.GAP, edges.get(0).getEditOperation()); // The ideal path should start with a gap
    assertEquals(EditOperation.NO_GAP, edges.get(1).getEditOperation());
    assertEquals(EditOperation.NO_GAP, edges.get(2).getEditOperation());
    assertEquals(EditOperation.NO_GAP, edges.get(3).getEditOperation());
    assertEquals(4, edges.size());
  }
  
  
  
  // TODO
  // All the witness are equal
  // There should only be one valid path through this decision graph
  @Ignore
  @Test
  public void testShortestPathEverythingEqual() {
    CollateXEngine engine = new CollateXEngine();
    IWitness a = engine.createWitness("a", "The red cat and the black cat");
    IWitness b = engine.createWitness("b", "The red cat and the black cat");
    EditGraphCreator creator = new EditGraphCreator();
    EditGraph dGraph = creator.buildEditGraph(a, b, new EqualityTokenComparator());
    EditGraphVisitor visitor = new EditGraphVisitor(dGraph);

    List<EditGraphEdge> path = visitor.getShortestPath();
    // we expect 8 edges
    // they all should have weight 0
    Iterator<EditGraphEdge> edges = path.iterator();
    assertEquals(new Integer(0), edges.next().getEditOperation());
  }

}
