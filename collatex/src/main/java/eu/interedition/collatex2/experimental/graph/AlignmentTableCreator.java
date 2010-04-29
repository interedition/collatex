package eu.interedition.collatex2.experimental.graph;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.alignmenttable.AlignmentTable4;
import eu.interedition.collatex2.implementation.alignmenttable.Column3;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IWitness;

public class AlignmentTableCreator {

  private final IVariantGraph graph;

  public AlignmentTableCreator(IVariantGraph graph) {
    this.graph = graph;
  }

  public IAlignmentTable getAlignmentTable() {
    IAlignmentTable table = new AlignmentTable4();
    if (graph.isEmpty()) {
      return table;
    }
    IWitness first = graph.getWitnesses().get(0);
    table.getSigli().add(first.getSigil());
    // this functionality should be moved to graph!
    IVariantGraphNode beginNode = graph.getStartNode();
    List<IVariantGraphNode> path = Lists.newArrayList();
    while (graph.hasArc(beginNode, first)) {
      IVariantGraphArc arc = graph.findArc(beginNode, first);
      IVariantGraphNode endNode = arc.getEndNode();
      path.add(endNode);
      beginNode = endNode;
    }
    //now use the path to fill the row ... 
    for (IVariantGraphNode node : path) {
      table.add(new Column3(node.getToken(), -1));
    }
    return table;
  }

}
