package eu.interedition.collatex.dekker.decision_tree;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.VariantGraph.Vertex;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.dekker.matrix.Island;
import eu.interedition.collatex.dekker.matrix.MatchTable;
import eu.interedition.collatex.jung.JungVariantGraph;
import eu.interedition.collatex.simple.SimpleWitness;

/*
 * Decision Tree
 * @author: Ronald Haentjens Dekker
 */
@SuppressWarnings("serial")
public class DecisionTree extends DirectedSparseGraph<DecisionNode, AlternativeEdge>{
  private final DecisionNode start;
  
  public DecisionTree() {
    this.start = new DecisionNode();
    addVertex(start);
  }

  public DecisionTree(SimpleWitness a, SimpleWitness b) {
    this();
    VariantGraph graph = new JungVariantGraph();
    addFirstWitnessToGraph(a, graph);
    createDecisionTree(graph, b);
  }

  private void createDecisionTree(VariantGraph graph, SimpleWitness b) {
    MatchTable table = MatchTable.create(graph, b);
    Multimap<Integer, Island> islandMultimap = ArrayListMultimap.create();
    for (Island isl : table.getIslands()) {
      islandMultimap.put(isl.size(), isl);
    }
    DecisionNode from = getStart();
    do {
      Integer max = Collections.max(islandMultimap.keySet());
      List<Island> possibleIslands = Lists.newArrayList(islandMultimap.get(max));
      if (possibleIslands.size()>1) {
        throw new RuntimeException("Not yet implemented!");
      }
      Island alternative = possibleIslands.get(0);
      DecisionNode to = addAlternative(alternative, from);
      islandMultimap.remove(max, alternative);
      from = to;
    } while (!islandMultimap.isEmpty());
  }

  private DecisionNode addAlternative(Island alternative, DecisionNode from) {
    DecisionNode node = new DecisionNode();
    AlternativeEdge edge = new AlternativeEdge(alternative);
    addVertex(node);
    addEdge(edge, from, node);
    return node;
  }

  public DecisionNode getStart() {
    return start;
  }
  
  //Note: method looks like CollationAlgorithm.Base merge (can't use that method
  // due to inheritance)
  private void addFirstWitnessToGraph(SimpleWitness a, VariantGraph graph) {
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
