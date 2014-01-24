package eu.interedition.collatex.dekker.decision_tree2;

import java.util.Collections;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Lists;

import eu.interedition.collatex.dekker.matrix.Island;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DekkerDecisionTreeAlgorithmTest {

  @Test
  public void testIsGoal() {
    ExtendedMatchTableSelection emptySelection = mock(ExtendedMatchTableSelection.class);
    when(emptySelection.getPossibleIslands()).thenReturn(Collections.<Island> emptyList());
    ExtendedMatchTableSelection someIslands = mock(ExtendedMatchTableSelection.class);
    when(someIslands.getPossibleIslands()).thenReturn(Collections.singletonList(new Island()));
    DekkerDecisionTreeAlgorithm algorithm = new DekkerDecisionTreeAlgorithm();
    DecisionTreeNode node = new DecisionTreeNode(emptySelection);
    DecisionTreeNode node2 = new DecisionTreeNode(someIslands);
    assertTrue(algorithm.isGoal(node));
    assertFalse(algorithm.isGoal(node2));
  }
  
  //First test that the first island from the graph and the witness
  //are the same
  @Test
  public void testNeighborNodes1() {
    Island i = new Island();
    ExtendedMatchTableSelection currentSelection = mock(ExtendedMatchTableSelection.class);
    ExtendedMatchTableSelection child1selection = mock(ExtendedMatchTableSelection.class);
    ExtendedMatchTableSelection child2selection = mock(ExtendedMatchTableSelection.class);
    when(currentSelection.getPossibleIslands()).thenReturn(Collections.singletonList(i));
    when(currentSelection.getFirstVectorFromGraph()).thenReturn(i);
    when(currentSelection.getFirstVectorFromWitness()).thenReturn(i);
    when(currentSelection.copy()).thenReturn(child1selection).thenReturn(child2selection);
    DekkerDecisionTreeAlgorithm algorithm = new DekkerDecisionTreeAlgorithm();
    DecisionTreeNode current = new DecisionTreeNode(currentSelection);
    Set<DecisionTreeNode> neighborNodes = algorithm.neighborNodes(current);
    verify(currentSelection, times(2)).copy();
    verify(child1selection).selectFirstVectorFromGraph();
    verify(child2selection).skipFirstVectorFromGraph();
    assertTrue(neighborNodes.contains(new DecisionTreeNode(child1selection)));
    assertTrue(neighborNodes.contains(new DecisionTreeNode(child2selection)));
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
    when(currentSelection.getPossibleIslands()).thenReturn(Lists.newArrayList(iGraph, iWitness));
    when(currentSelection.getFirstVectorFromGraph()).thenReturn(iGraph);
    when(currentSelection.getFirstVectorFromWitness()).thenReturn(iWitness);
    when(currentSelection.copy()).thenReturn(child1selection).thenReturn(child2selection).thenReturn(child3selection).thenReturn(child4selection);
    DekkerDecisionTreeAlgorithm algorithm = new DekkerDecisionTreeAlgorithm();
    DecisionTreeNode current = new DecisionTreeNode(currentSelection);
    Set<DecisionTreeNode> neighborNodes = algorithm.neighborNodes(current);
    verify(currentSelection, times(4)).copy();
    verify(child1selection).selectFirstVectorGraphTransposeWitness();
    verify(child2selection).selectFirstVectorWitnessTransposeGraph();
    verify(child3selection).skipFirstVectorFromGraph();
    verify(child4selection).skipFirstVectorFromWitness();
    assertTrue(neighborNodes.contains(new DecisionTreeNode(child1selection)));
    assertTrue(neighborNodes.contains(new DecisionTreeNode(child2selection)));
    assertTrue(neighborNodes.contains(new DecisionTreeNode(child3selection)));
    assertTrue(neighborNodes.contains(new DecisionTreeNode(child4selection)));
  }
}
