package eu.interedition.collatex.dekker.decision_tree2;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.matrix.MatchTable;

/*
 * Alignment algorithm based on a decision tree with a fixed number of children per node.
 * 
 * @author: Ronald Haentjens Dekker
 */

public class DekkerDecisionTreeAlgorithm extends CollationAlgorithm.Base {
  protected List<DecisionTreeNode> possibleAlignments;

  @Override
  public void collate(VariantGraph against, Iterable<Token> witness) {
    // temporary code to add the first witness to the graph
    // without having to actually align them.
    if (against.witnesses().isEmpty()) {
      VariantGraphBuilder.addFirstWitnessToGraph(against, witness);
      return;
    }
    MatchTable table = MatchTable.create(against, witness);
    ExtendedMatchTableSelection selection = new ExtendedMatchTableSelection(table);
    DecisionTreeNode root = new DecisionTreeNode(selection);
    possibleAlignments = Lists.newArrayList(root);
  }

  void listPossibleAlignments() {
    for (DecisionTreeNode alignment : possibleAlignments) {
      System.out.println("Aligned tokens: "+alignment.getNumberOfAlignedTokens()+", Gap tokens: "+alignment.getNumberOfGapTokens()+", skipped islands: "+alignment.hasSkippedIslands()+", is finished: "+alignment.isFinished()+". "+alignment.log());
    }
    System.out.println("----");
  }

  void expandPossibleAlignments() {
    List<DecisionTreeNode> result = Lists.newArrayList();
    for (DecisionTreeNode alignment : possibleAlignments) {
      result.addAll(alignment.getChildNodes());
    }
    possibleAlignments = result;
  }
}
