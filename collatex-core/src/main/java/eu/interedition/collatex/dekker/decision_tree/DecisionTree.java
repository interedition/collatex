package eu.interedition.collatex.dekker.decision_tree;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import eu.interedition.collatex.dekker.matrix.Island;

/*
 * Decision Tree
 * @author: Ronald Haentjens Dekker
 */
@SuppressWarnings("serial")
public class DecisionTree extends DirectedSparseGraph<DecisionNode, AlternativeEdge>{
  private final DecisionNode start;
  
  public DecisionTree() {
    this.start = new DecisionNode(this.vertices.size());
    addVertex(start);
  }

  public DecisionNode addAlternative(Island alternative, DecisionNode from) {
    DecisionNode node = new DecisionNode(this.vertices.size());
    AlternativeEdge edge = new AlternativeEdge(this, alternative);
    addVertex(node);
    addEdge(edge, from, node);
    return node;
  }

  public DecisionNode getStart() {
    return start;
  }
  
}
