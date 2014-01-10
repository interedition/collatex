package eu.interedition.collatex.dekker.decision_tree;

import static eu.interedition.collatex.dekker.decision_tree.VariantGraphBuilder.addFirstWitnessToGraph;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.matrix.Island;
import eu.interedition.collatex.dekker.matrix.MatchTable;
import eu.interedition.collatex.dekker.matrix.MatchTableSelection;
import eu.interedition.collatex.jung.JungVariantGraph;
import eu.interedition.collatex.simple.SimpleWitness;

/*
 * This class contains functions that convert a vectorspace 
 * into a decisiontree / decisiongraph
 * with which the optimal alignment (given a scoring function)
 * can be calculated
 * 
 * @author: Ronald Haentjens Dekker
 */

public class DecisionTreeCreator {
  public static DecisionTree createDecisionTree(SimpleWitness a, SimpleWitness b) {
    VariantGraph graph = new JungVariantGraph();
    addFirstWitnessToGraph(graph, a);
    return createDecisionTree(graph, b);
  }


  public static DecisionTree createDecisionTree(VariantGraph graph, SimpleWitness b) {
    DecisionTree tree = new DecisionTree();
    MatchTable table = MatchTable.create(graph, b);
    MatchTableSelection selection = new MatchTableSelection(table);
    DecisionNode from = tree.getStart();
    do {
      List<DecisionNode> createdNodes = createNodesForPossibleIslands(selection, tree, from);
      //TODO: The from node should be the optimal nodes of
      //the created nodes; for now we select the first one
      from = createdNodes.get(0);
      //Note: possible islands are fetched twice here!
    } while (!selection.getPossibleIslands().isEmpty());
    return tree;
  }

  private static List<DecisionNode> createNodesForPossibleIslands(MatchTableSelection selection, DecisionTree tree, DecisionNode from) {
    List<Island> possibleIslands = selection.getPossibleIslands();
    List<DecisionNode> createdNodes = Lists.newArrayList();
    for (Island alternative : possibleIslands) {
      DecisionNode to = tree.addAlternative(alternative, from);
      createdNodes.add(to);
      //TODO: this is a bit too broad: only selected island should be counted!
      selection.addIsland(alternative);
    }
    return createdNodes;
  }
}
