package eu.interedition.collatex2.experimental.graph;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class AlignmentGraph implements IAlignmentGraph {
  private final List<IAlignmentNode> nodes;
  private final IAlignmentNode startNode;
//  private final List<IAlignmentArc> arcs;

  public static AlignmentGraph create() {
    final AlignmentGraph graph = new AlignmentGraph();
    return graph;
  }
  
  public static IAlignmentGraph create(IWitness a) {
    AlignmentGraph graph = create();
    for (INormalizedToken token : a.getTokens()) {
      graph.add(new AlignmentNode(token.getNormalized()));
    }
    return graph;
  }

  @Override
  public List<IAlignmentNode> getNodes() {
    return nodes;
  }

  @Override
  public IAlignmentNode getStartNode() {
    return startNode;
  }

//  @Override
//  public List<IAlignmentArc> getArcs() {
//    return arcs;
//  }

  private AlignmentGraph() {
    this.nodes = Lists.newArrayList();
//    this.arcs = Lists.newArrayList();
    final IAlignmentNode startNode = new AlignmentNode("#");
    add(startNode);
    this.startNode = startNode;
  }

  private void add(IAlignmentNode alignmentNode) {
    nodes.add(alignmentNode);
  }


}
