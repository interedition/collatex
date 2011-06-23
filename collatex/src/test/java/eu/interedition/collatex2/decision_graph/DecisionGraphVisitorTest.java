package eu.interedition.collatex2.decision_graph;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.decision_graph.DGEdge;
import eu.interedition.collatex2.implementation.decision_graph.DecisionGraph;
import eu.interedition.collatex2.implementation.decision_graph.DecisionGraphCreator;
import eu.interedition.collatex2.implementation.decision_graph.DecisionGraphVisitor;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

public class DecisionGraphVisitorTest {
  
  // All the witness are equal
  // There are choices to be made however, since there is duplication of tokens
  // Optimal alignment has no gaps
  @Test
  public void testGapsEverythingEqual() {
    CollateXEngine engine = new CollateXEngine();
    IWitness a = engine.createWitness("a", "The red cat and the black cat");
    IWitness b = engine.createWitness("b", "The red cat and the black cat");
    IVariantGraph vGraph = engine.graph(a);
    DecisionGraph dGraph = DecisionGraphCreator.buildDecisionGraph(vGraph, b);
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
    DecisionGraph dGraph = DecisionGraphCreator.buildDecisionGraph(graph, b);
    DecisionGraphVisitor.determineMinimumNumberOfGaps(dGraph);
    assertEquals(1, DecisionGraphVisitor.determineMinimumNumberOfGaps(dGraph));
  }

  // first make a unit test which strips down the decision graph
  @Test
  public void testXXX() {
    
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
    DecisionGraph dGraph = DecisionGraphCreator.buildDecisionGraph(vGraph, b);
    DecisionGraphVisitor visitor = new DecisionGraphVisitor(dGraph);
    
    List<DGEdge> path = visitor.getShortestPath(); 
    // we expect 8 edges
    // they all should have weight 0
    Iterator<DGEdge> edges = path.iterator();
    assertEquals(new Integer(0), edges.next().getWeight());
    
  } 
 
}
