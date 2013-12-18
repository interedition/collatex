package eu.interedition.collatex.dekker.decision_tree;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.VariantGraph.Vertex;
import eu.interedition.collatex.dekker.matrix.Island;
import eu.interedition.collatex.dekker.matrix.MatchTable;
import eu.interedition.collatex.dekker.matrix.MatchTableModifier;
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
    addFirstWitnessToGraph(a, graph);
    return createDecisionTree(graph, b);
  }


  public static DecisionTree createDecisionTree(VariantGraph graph, SimpleWitness b) {
    DecisionTree tree = new DecisionTree();
    MatchTable table = MatchTable.create(graph, b);
    Multimap<Integer, Island> islandMultimap = ArrayListMultimap.create();
    for (Island isl : table.getIslands()) {
      islandMultimap.put(isl.size(), isl);
    }
    DecisionNode from = tree.getStart();
    do {
      Integer max = Collections.max(islandMultimap.keySet());
      MatchTableModifier.removeOrSplitImpossibleIslands(table, max, islandMultimap);
      List<DecisionNode> createdNodes = createNodesForPossibleIslands(table, tree, islandMultimap, from, max);
      //TODO: The from node should be the optimal nodes of
      //the created nodes; for now we select the first one
      from = createdNodes.get(0);
    } while (!islandMultimap.isEmpty());
    return tree;
  }

  private static List<DecisionNode> createNodesForPossibleIslands(MatchTable table, DecisionTree tree, Multimap<Integer, Island> islandMultimap, DecisionNode from, Integer max) {
    List<Island> possibleIslands = Lists.newArrayList(islandMultimap.get(max));
    List<DecisionNode> createdNodes = Lists.newArrayList();
    for (Island alternative : possibleIslands) {
      DecisionNode to = tree.addAlternative(alternative, from);
      createdNodes.add(to);
      //TODO: this is a bit too broad: only selected island should be counted!
      islandMultimap.remove(max, alternative);
      table.commitIsland(alternative);
    }
    return createdNodes;
  }

  //Note: method looks like CollationAlgorithm.Base merge (can't use that method
  // due to inheritance)
  private static void addFirstWitnessToGraph(SimpleWitness a, VariantGraph graph) {
    List<Token> tokens = a.getTokens();
    Vertex from = graph.getStart();
    for (Token token : tokens) {
      Vertex to = graph.add(token);
      graph.connect(from, to, Sets.newHashSet((Witness)a));
      from = to;
    }
    graph.connect(from, graph.getEnd(), Sets.newHashSet((Witness)a));
  }

}
