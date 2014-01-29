package eu.interedition.collatex.dekker.decision_tree2;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.matrix.Coordinate;
import eu.interedition.collatex.dekker.matrix.Island;
import eu.interedition.collatex.dekker.matrix.MatchTable;

/*
 * TO BE DELETED!
 * Alignment algorithm based on a decision tree with a fixed number of children per node.
 * 
 * @author: Ronald Haentjens Dekker
 */

public class PreviousDekkerDecisionTreeAlgorithm extends CollationAlgorithm.Base {
  protected List<PreviousDecisionTreeNode> possibleAlignments;

  @Override
  public void collate(VariantGraph against, Iterable<Token> witness) {
    // temporary code to add the first witness to the graph
    // without having to actually align them.
    if (against.witnesses().isEmpty()) {
      VariantGraphBuilder.addFirstWitnessToGraph(against, witness);
      return;
    }
    System.out.println("Building MatchTable");
    MatchTable table = MatchTable.create(against, witness);
    ExtendedMatchTableSelection selection = new ExtendedMatchTableSelection(table);
    PreviousDecisionTreeNode root = new PreviousDecisionTreeNode(selection);
    possibleAlignments = Lists.newArrayList(root);
    System.out.println("Aligning...");
    align();
    System.out.println("Merging...");
    PreviousDecisionTreeNode alignment = selectOptimalCandidate();
    Map<Token, VariantGraph.Vertex> map = Maps.newHashMap();
    for (Island island : alignment.getIslands()) {
      for (Coordinate c : island) {
        map.put(table.tokenAt(c.getRow(), c.getColumn()), table.vertexAt(c.getRow(), c.getColumn()));
      }
    }
    merge(against, witness, map);
    System.out.println("DONE");
  }

  private PreviousDecisionTreeNode selectOptimalCandidate() {
    PreviousDecisionTreeNode best = possibleAlignments.get(0);
    for (PreviousDecisionTreeNode alignment : possibleAlignments) {
      if (alignment.getNumberOfGapTokens() < best.getNumberOfGapTokens()) {
        best = alignment;
      }
    }
    return best;
  }

  private void align() {
    boolean unfinishedAlignmentFound;
    int numberOfLoops = 0;
    do {
      if (numberOfLoops > 5) {
        System.out.println("Limit reached!");
        break;
      }
      System.out.println("Loop number: "+numberOfLoops+" possibilities: "+possibleAlignments.size());
      expandPossibleAlignments();
      numberOfLoops++;
      // look for an unfinished candidate alignment
      unfinishedAlignmentFound = false;
      for (PreviousDecisionTreeNode alignment : possibleAlignments) {
        if (!alignment.isFinished()) {
          unfinishedAlignmentFound = true;
          break;
        }
      }
    } while (unfinishedAlignmentFound);
    System.out.println(String.format("Number of alignment considered: %s", possibleAlignments.size()));
  }

  void listPossibleAlignments() {
    for (PreviousDecisionTreeNode alignment : possibleAlignments) {
      System.out.println("AT: "+alignment.getNumberOfAlignedTokens()+", GT: "+alignment.getNumberOfGapTokens()+", TT: "+alignment.getNumberOfTransposedTokens()+", skipped islands: "+alignment.hasSkippedIslands()+", is finished: "+alignment.isFinished()+". "+alignment.log());
    }
    System.out.println("----");
  }

  void expandPossibleAlignments() {
    //TEMP WORKAROUND!
    DekkerDecisionTreeAlgorithm algo = new DekkerDecisionTreeAlgorithm();
    List<PreviousDecisionTreeNode> result = Lists.newArrayList();
    for (PreviousDecisionTreeNode alignment : possibleAlignments) {
      algo.associate(alignment, alignment.selection);
      for (DecisionTreeNode child : algo.neighborNodes(alignment)) {
        ExtendedMatchTableSelection extendedMatchTableSelection = algo.selection.get(child);
        result.add(new PreviousDecisionTreeNode(extendedMatchTableSelection));
      }
    }  
    possibleAlignments = result;
  }
}
