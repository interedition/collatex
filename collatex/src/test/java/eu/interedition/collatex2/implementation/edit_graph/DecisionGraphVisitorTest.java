package eu.interedition.collatex2.implementation.edit_graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.edit_graph.DGEdge;
import eu.interedition.collatex2.implementation.edit_graph.DGVertex;
import eu.interedition.collatex2.implementation.edit_graph.DecisionGraph;
import eu.interedition.collatex2.implementation.edit_graph.DecisionGraphCreator;
import eu.interedition.collatex2.implementation.edit_graph.DecisionGraphVisitor;
import eu.interedition.collatex2.implementation.edit_graph.VariantGraphMatcher;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

public class DecisionGraphVisitorTest {

  public void assertVertices(DecisionGraph dGraph, String... normalized) {
    Iterator<DGVertex> topologicIterator = dGraph.iterator();
    for (String expectedNormalized : normalized) {
      assertTrue("not enough vertices!", topologicIterator.hasNext());
      assertEquals(expectedNormalized, topologicIterator.next().getToken().getNormalized());
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
    IVariantGraph vGraph = engine.graph(a);
    VariantGraphMatcher matcher = new VariantGraphMatcher();
    DecisionGraphCreator creator = new DecisionGraphCreator(matcher, vGraph, b);
    DecisionGraph dGraph = creator.buildDecisionGraph();
    assertEquals(0, DecisionGraphVisitor.determineMinimumNumberOfGaps(dGraph));
  }

  // There is an omission
  // Optimal alignment has 1 gap
  // Note: there are two paths here that contain 1 gap
  @Test
  public void testGapsOmission() {
    CollateXEngine engine = new CollateXEngine();
    IWitness a = engine.createWitness("a", "The red cat and the black cat");
    IWitness b = engine.createWitness("b", "the black cat");
    IVariantGraph graph = engine.graph(a);
    VariantGraphMatcher matcher = new VariantGraphMatcher();
    DecisionGraphCreator creator = new DecisionGraphCreator(matcher, graph, b);
    DecisionGraph dGraph = creator.buildDecisionGraph();
    assertEquals(1, DecisionGraphVisitor.determineMinimumNumberOfGaps(dGraph));
  }

  // first make a unit test which strips down the decision graph
  @Test
  public void testRemoveChoicesThatIntroduceGaps() {
    CollateXEngine engine = new CollateXEngine();
    IWitness a = engine.createWitness("a", "The red cat and the black cat");
    IWitness b = engine.createWitness("b", "the black cat");
    IVariantGraph graph = engine.graph(a);
    VariantGraphMatcher matcher = new VariantGraphMatcher();
    DecisionGraphCreator creator = new DecisionGraphCreator(matcher, graph, b);
    DecisionGraph dGraph = creator.buildDecisionGraph();
    DecisionGraphVisitor visitor = new DecisionGraphVisitor(dGraph);
    DecisionGraph dGraph2 = visitor.removeChoicesThatIntroduceGaps();
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
    IVariantGraph graph = engine.graph(a);
    VariantGraphMatcher matcher = new VariantGraphMatcher();
    DecisionGraphCreator creator = new DecisionGraphCreator(matcher, graph, b);
    DecisionGraph dGraph = creator.buildDecisionGraph();
    DecisionGraphVisitor visitor = new DecisionGraphVisitor(dGraph);
    DecisionGraph dGraph2 = visitor.removeChoicesThatIntroduceGaps();
    Map<DGVertex, Integer> determineMinSequences = visitor.determineMinSequences(dGraph2);
    // asserts
    Iterator<DGVertex> dgVerticesIterator = dGraph2.iterator();
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
    IVariantGraph graph = engine.graph(a);
    VariantGraphMatcher matcher = new VariantGraphMatcher();
    DecisionGraphCreator creator = new DecisionGraphCreator(matcher, graph, b);
    DecisionGraph dGraph = creator.buildDecisionGraph();
    DecisionGraphVisitor visitor = new DecisionGraphVisitor(dGraph);
    List<DGEdge> edges = visitor.getShortestPath();
    assertTrue(edges.get(0).getWeight()==1); // The ideal path should start with a gap
    assertTrue(edges.get(1).getWeight()==0);
    assertTrue(edges.get(2).getWeight()==0);
    assertTrue(edges.get(3).getWeight()==0);
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
    IVariantGraph vGraph = engine.graph(a);
    VariantGraphMatcher matcher = new VariantGraphMatcher();
    DecisionGraphCreator creator = new DecisionGraphCreator(matcher, vGraph, b);
    DecisionGraph dGraph = creator.buildDecisionGraph();
    DecisionGraphVisitor visitor = new DecisionGraphVisitor(dGraph);

    List<DGEdge> path = visitor.getShortestPath();
    // we expect 8 edges
    // they all should have weight 0
    Iterator<DGEdge> edges = path.iterator();
    assertEquals(new Integer(0), edges.next().getWeight());
  }

}
