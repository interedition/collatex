package eu.interedition.collatex.implementation.graph.edit;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.implementation.graph.EditGraph;
import eu.interedition.collatex.implementation.graph.EditGraphCreator;
import eu.interedition.collatex.implementation.graph.EditGraphEdge;
import eu.interedition.collatex.implementation.graph.EditGraphVertex;
import eu.interedition.collatex.implementation.graph.EditGraphVisitor;
import eu.interedition.collatex.implementation.graph.EditOperation;
import eu.interedition.collatex.implementation.input.SimpleToken;
import eu.interedition.collatex.implementation.matching.EqualityTokenComparator;
import eu.interedition.collatex.implementation.matching.Matches;
import eu.interedition.collatex.implementation.output.DotExporter;
import eu.interedition.collatex.interfaces.IWitness;

import org.junit.Ignore;
import org.junit.Test;

public class EditGraphVisitorTest extends AbstractTest {

  public void assertVertices(EditGraph dGraph, String... normalized) {
    Iterator<EditGraphVertex> topologicIterator = dGraph.iterator();
    for (String expectedNormalized : normalized) {
      assertTrue("not enough vertices!", topologicIterator.hasNext());
      assertEquals(expectedNormalized, ((SimpleToken) topologicIterator.next().getBaseToken()).getNormalized());
    }
  }

  // All the witness are equal
  // There are choices to be made however, since there is duplication of tokens
  // Optimal alignment has no gaps
  @Test
  public void testGapsEverythingEqual() {
    final IWitness[] w = createWitnesses("The red cat and the black cat", "The red cat and the black cat");
    EditGraphCreator creator = new EditGraphCreator();
    EditGraph dGraph = creator.buildEditGraph(w[0], w[1], new EqualityTokenComparator());
    assertEquals(0, EditGraphVisitor.determineMinimumNumberOfGaps(dGraph));
  }

  // There is an omission
  // Optimal alignment has 1 gap
  // Note: there are two paths here that contain 1 gap
  @Test
  public void testGapsOmission() {
    final IWitness[] w = createWitnesses("The red cat and the black cat", "the black cat");
    EditGraphCreator creator = new EditGraphCreator();
    EditGraph dGraph = creator.buildEditGraph(w[0], w[1], new EqualityTokenComparator());
    assertEquals(1, EditGraphVisitor.determineMinimumNumberOfGaps(dGraph));
  }

  // first make a unit test which strips down the decision graph
  @Test
  public void testRemoveChoicesThatIntroduceGaps() {
    final IWitness[] w = createWitnesses("The red cat and the black cat", "the black cat");
    EditGraphCreator creator = new EditGraphCreator();
    EqualityTokenComparator comparator = new EqualityTokenComparator();
    EditGraph dGraph = creator.buildEditGraph(w[0], w[1], comparator);
    EditGraphVisitor visitor = new EditGraphVisitor(dGraph);
    Matches matches = Matches.between(w[0], w[1], comparator);
    EditGraph dGraph2 = visitor.removeChoicesThatIntroduceGaps(matches);
    DotExporter.generateSVG("test.svg", DotExporter.toDot(dGraph2), "The red cat and the black cat");
    // I expect 6 vertices
    // start, 2 x the, black, cat en end
    assertVertices(dGraph2, "#", "the", "the", "black", "cat", "#");
  }

  //When there are multiple paths with the same minimum number of gaps
  //do a second pass that tries to find the longest common sequence
  @Test
  public void testTryToFindMinimumAmountOfSequences() {
    final IWitness[] w = createWitnesses("The red cat and the black cat", "the black cat");
    EditGraphCreator creator = new EditGraphCreator();
    EqualityTokenComparator comparator = new EqualityTokenComparator();
    EditGraph dGraph = creator.buildEditGraph(w[0], w[1], comparator);
    EditGraphVisitor visitor = new EditGraphVisitor(dGraph);
    Matches matches = Matches.between(w[0], w[1], comparator);
    EditGraph dGraph2 = visitor.removeChoicesThatIntroduceGaps(matches);
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
    final IWitness[] w = createWitnesses("The red cat and the black cat", "the black cat");
    EditGraphCreator creator = new EditGraphCreator();
    EqualityTokenComparator comparator = new EqualityTokenComparator();
    EditGraph dGraph = creator.buildEditGraph(w[0], w[1], comparator);
    EditGraphVisitor visitor = new EditGraphVisitor(dGraph);
    Matches matches = Matches.between(w[0], w[1], comparator);
    List<EditGraphEdge> edges = visitor.getShortestPath(matches);
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
    final IWitness[] w = createWitnesses("The red cat and the black cat", "The red cat and the black cat");
    EditGraphCreator creator = new EditGraphCreator();
    EqualityTokenComparator comparator = new EqualityTokenComparator();
    EditGraph dGraph = creator.buildEditGraph(w[0], w[1], comparator);
    EditGraphVisitor visitor = new EditGraphVisitor(dGraph);

    Matches matches = Matches.between(w[0], w[1], comparator);
    List<EditGraphEdge> path = visitor.getShortestPath(matches);
    // we expect 8 edges
    // they all should have weight 0
    Iterator<EditGraphEdge> edges = path.iterator();
    assertEquals(new Integer(0), edges.next().getEditOperation());
  }

}
