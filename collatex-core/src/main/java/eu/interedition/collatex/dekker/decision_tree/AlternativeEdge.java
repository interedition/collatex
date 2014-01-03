package eu.interedition.collatex.dekker.decision_tree;

import eu.interedition.collatex.dekker.matrix.Island;

// @author: Ronald Haentjens Dekker
public class AlternativeEdge {
	private DecisionNode parent;
	private Cost cost;
  private Island island;
  private DecisionTree tree;
  
  public AlternativeEdge(DecisionNode parent, Cost cost) {
    this.parent = parent;
    this.cost = cost;
  }

  public AlternativeEdge(DecisionTree tree, Island island) {
    this.tree = tree;
    this.island = island;
  }
  
  public DecisionNode getSource() {
    if (parent!=null) {
      return parent;
    }
    if (tree==null) {
      throw new RuntimeException("Parent and tree are not set!");
    }
    return tree.getSource(this);
  }

  public Island getIsland() {
    if (island==null) {
      throw new RuntimeException("Island is not set!");
    }
    return island;
  }

  public Cost getCost() {
    if (cost==null) {
      cost = calculateCost();
    }
    return cost;
  }

  private Cost calculateCost() {
    if (island == null) {
      throw new RuntimeException("Can't calculate cost, island not set!");
    }
    return new Cost(island);
  }

  @Override
  public String toString() {
    DecisionNode source = getSource();
    //DecisionNode dest = tree.getDest(this);
    return source+"[Cost: "+getCost().toString()+"]";
    //return source+"-["+(island.getLeftEnd().getColumn()+1)+","+(island.getLeftEnd().getRow()+1)+",l:"+island.size()+"]->"+dest;
  }

}
