package eu.interedition.collatex.dekker.decision_tree;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class DecisionTreeTraversal {

  public static void traverseTree(DecisionTree dt, Function<AlternativeEdge, Void> f) {
    List<DecisionNode> nodesToTraverse = Lists.newArrayList();
    nodesToTraverse.add(dt.getStart());
    while(!nodesToTraverse.isEmpty()) {
      DecisionNode n = nodesToTraverse.remove(0);
      determineChildrenAndAddToList(dt, n, nodesToTraverse);
      handleNode(dt, n, f);
    }
  }

  public static void determineChildrenAndAddToList(DecisionTree tree, DecisionNode n, List<DecisionNode> nodesToTraverse) {
    List<DecisionNode> childrenToTraverse = Lists.newArrayList();
    for (AlternativeEdge e :tree.getOutEdges(n)) {
      DecisionNode dest = tree.getDest(e);
      childrenToTraverse.add(dest);
    }
    nodesToTraverse.addAll(0, childrenToTraverse);
  }

  public static void handleNode(DecisionTree dt, DecisionNode n, Function<AlternativeEdge, Void> f) {
    for (AlternativeEdge e: dt.getOutEdges(n)) {
      f.apply(e);
    }
  }

}
