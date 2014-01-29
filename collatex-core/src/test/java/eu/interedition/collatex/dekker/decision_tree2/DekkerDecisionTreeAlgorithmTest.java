package eu.interedition.collatex.dekker.decision_tree2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Lists;

import eu.interedition.collatex.dekker.matrix.Island;

public class DekkerDecisionTreeAlgorithmTest {

  @Test
  public void testIsGoal() {
    ExtendedMatchTableSelection selection = mock(ExtendedMatchTableSelection.class);
    DekkerDecisionTreeAlgorithm algorithm = new DekkerDecisionTreeAlgorithm();
    DecisionTreeNode node = new DecisionTreeNode();
    algorithm.associate(node, selection);
    algorithm.isGoal(node);
    verify(selection).isFinished();
  }
  
  //First test that the first island from the graph and the witness
  //are the same
  @Test
  public void testNeighborNodes1() {
    Island i = new Island();
    ExtendedMatchTableSelection currentSelection = mock(ExtendedMatchTableSelection.class);
    ExtendedMatchTableSelection child1selection = mock(ExtendedMatchTableSelection.class);
    ExtendedMatchTableSelection child2selection = mock(ExtendedMatchTableSelection.class);
    when(currentSelection.isFinished()).thenReturn(false);
    when(currentSelection.getFirstVectorFromGraph()).thenReturn(i);
    when(currentSelection.getFirstVectorFromWitness()).thenReturn(i);
    when(currentSelection.copy()).thenReturn(child1selection).thenReturn(child2selection);
    DekkerDecisionTreeAlgorithm algorithm = new DekkerDecisionTreeAlgorithm();
    DecisionTreeNode current = new DecisionTreeNode();
    algorithm.associate(current, currentSelection);
    Set<DecisionTreeNode> neighborNodes = algorithm.neighborNodes(current);
    verify(currentSelection, times(2)).copy();
    verify(child1selection).selectFirstVectorFromGraph();
    verify(child2selection).skipFirstVectorFromGraph();
    Collection<ExtendedMatchTableSelection> values = algorithm.selection.values();
    assertTrue(values.contains(child1selection));
    assertTrue(values.contains(child2selection));
    assertEquals(2, neighborNodes.size());
  }
  
  //Second test that the first island from the graph and the witness
  //are not the same
  @Test
  public void testNeighborNodes2() {
    Island iGraph = new Island();
    Island iWitness = new Island();
    ExtendedMatchTableSelection currentSelection = mock(ExtendedMatchTableSelection.class);
    ExtendedMatchTableSelection child1selection = mock(ExtendedMatchTableSelection.class);
    ExtendedMatchTableSelection child2selection = mock(ExtendedMatchTableSelection.class);
    ExtendedMatchTableSelection child3selection = mock(ExtendedMatchTableSelection.class);
    ExtendedMatchTableSelection child4selection = mock(ExtendedMatchTableSelection.class);
    when(currentSelection.isFinished()).thenReturn(false);
    when(currentSelection.getFirstVectorFromGraph()).thenReturn(iGraph);
    when(currentSelection.getFirstVectorFromWitness()).thenReturn(iWitness);
    when(currentSelection.copy()).thenReturn(child1selection).thenReturn(child2selection).thenReturn(child3selection).thenReturn(child4selection);
    DekkerDecisionTreeAlgorithm algorithm = new DekkerDecisionTreeAlgorithm();
    DecisionTreeNode current = new DecisionTreeNode();
    algorithm.associate(current, currentSelection);
    Set<DecisionTreeNode> neighborNodes = algorithm.neighborNodes(current);
    verify(currentSelection, times(4)).copy();
    verify(child1selection).selectFirstVectorGraphTransposeWitness();
    verify(child2selection).selectFirstVectorWitnessTransposeGraph();
    verify(child3selection).skipFirstVectorFromGraph();
    verify(child4selection).skipFirstVectorFromWitness();
    Collection<ExtendedMatchTableSelection> values = algorithm.selection.values();
    assertTrue(values.contains(child1selection));
    assertTrue(values.contains(child2selection));
    assertTrue(values.contains(child3selection));
    assertTrue(values.contains(child4selection));
    assertEquals(4, neighborNodes.size());
  }
}
