package eu.interedition.collatex.dekker.decision_tree2;

import static eu.interedition.collatex.dekker.decision_tree2.VariantGraphBuilder.addFirstWitnessToGraph;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.matrix.Island;
import eu.interedition.collatex.dekker.matrix.MatchTable;
import eu.interedition.collatex.jung.JungVariantGraph;
import eu.interedition.collatex.simple.SimpleWitness;

/*
 * DecisionTreeNode class
 * 
 * @Author: Ronald Haentjens Dekker
 * 
 * Every node has a fixed number of children.
 * 4 for progressive alignment.
 */
public class DecisionTreeNode {

  private ExtendedMatchTableSelection selection;

  public DecisionTreeNode(ExtendedMatchTableSelection selection) {
    this.selection = selection;
  }

  public static DecisionTreeNode createDecisionTree(SimpleWitness a, SimpleWitness b) {
    VariantGraph graph = new JungVariantGraph();
    addFirstWitnessToGraph(graph, a);
    return createDecisionTree(graph, b);
  }

  public static DecisionTreeNode createDecisionTree(VariantGraph graph, SimpleWitness b) {
    MatchTable table = MatchTable.create(graph, b);
    ExtendedMatchTableSelection selection = new ExtendedMatchTableSelection(table);
    DecisionTreeNode root = new DecisionTreeNode(selection);
    return root;
  }
  
  public List<DecisionTreeNode> calculateAlternatives() {
    List<DecisionTreeNode> childNodes = Lists.newArrayList();
    // make 4 copies
    ExtendedMatchTableSelection copy1 = new ExtendedMatchTableSelection(selection);
    ExtendedMatchTableSelection copy2 = new ExtendedMatchTableSelection(selection);
    ExtendedMatchTableSelection copy3 = new ExtendedMatchTableSelection(selection);
    ExtendedMatchTableSelection copy4 = new ExtendedMatchTableSelection(selection);
    childNodes.add(copy1.selectFirstVectorFromGraph());
    childNodes.add(copy2.selectFirstVectorFromWitness());
    childNodes.add(copy3.skipFirstVectorFromGraph());
    childNodes.add(copy4.skipFirstVectorFromWitness());
    return childNodes;
  }

  public int getNumberOfAlignedTokens() {
    List<Island> islands = selection.getIslands();
    int numberOfSelectedTokens = 0;
    for (Island selected : islands) {
      numberOfSelectedTokens += selected.size();
    }
    return numberOfSelectedTokens;
  }

  //TODO: this implementation is too simple
  //TODO: one should take the delta since last aligned token into account
  public int getNumberOfGapTokens() {
    if (selection.getPossibleIslands().isEmpty()) {
      Island lastIsland = selection.getIslands().get(selection.getIslands().size()-1);
      int column = lastIsland.getRightEnd().getColumn() +1;
      return column - getNumberOfAlignedTokens();
    } else {
      Island nextIsland = selection.getFirstVectorFromGraph();
      int column = nextIsland.getLeftEnd().getColumn();
      return column - getNumberOfAlignedTokens();
    }
  }

  public int getNumberOfSelectedVectors() {
    return selection.getIslands().size();
  }
}
