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
  private DecisionTreeNode root;

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
    root = new DecisionTreeNode(selection);
    // TODO: add comparison of cost for each of the alternatives
    List<DecisionTreeNode> possibleAlignments = Lists.newArrayList();
    possibleAlignments.add(root);
    List<DecisionTreeNode> more = expandPossibleAlignments(possibleAlignments);
    listPossibleAlignments(expandPossibleAlignments(more));
  }

  private void listPossibleAlignments(List<DecisionTreeNode> more) {
    for (DecisionTreeNode alignment : more) {
      System.out.println("Aligned tokens: "+alignment.getNumberOfAlignedTokens()+", Gap tokens: "+alignment.getNumberOfGapTokens()+", skipped islands: "+alignment.hasSkippedIslands());
    }
    System.out.println("----");
  }

  private List<DecisionTreeNode> expandPossibleAlignments(List<DecisionTreeNode> possibleAlignments) {
    List<DecisionTreeNode> result = Lists.newArrayList();
    for (DecisionTreeNode alignment : possibleAlignments) {
      result.addAll(alignment.getChildNodes());
    }
    return result;
  }

  public DecisionTreeNode getRoot() {
    if (root == null) {
      throw new RuntimeException("Nothing has been collated yet! No decision tree has been created!");
    }
    return root;
  }
}
