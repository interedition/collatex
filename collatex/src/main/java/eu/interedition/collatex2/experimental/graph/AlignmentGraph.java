package eu.interedition.collatex2.experimental.graph;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class AlignmentGraph implements IAlignmentGraph {
  private final List<IAlignmentNode> nodes;
  private final IAlignmentNode startNode;
  private final List<IAlignmentArc> arcs;

  public static AlignmentGraph create() {
    final AlignmentGraph graph = new AlignmentGraph();
    return graph;
  }
  
  public static IAlignmentGraph create(IWitness a) {
    AlignmentGraph graph = create();
    List<IAlignmentNode> newNodes = Lists.newArrayList();
    for (INormalizedToken token : a.getTokens()) {
      //TODO: maybe make this graph.addNewNode(token)?
      //TODO: use it in constructor too!
      final AlignmentNode newNode = new AlignmentNode(token.getNormalized());
      newNodes.add(newNode);
      graph.add(newNode);
    }
    IAlignmentNode previous = graph.getStartNode();
    for (IAlignmentNode node : newNodes) {
      graph.addNewArc(previous, node, a);
      previous = node;
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

  @Override
  public List<IAlignmentArc> getArcs() {
    return arcs;
  }

  private AlignmentGraph() {
    this.nodes = Lists.newArrayList();
    this.arcs = Lists.newArrayList();
    final IAlignmentNode startNode = new AlignmentNode("#");
    add(startNode);
    this.startNode = startNode;
  }

  private void add(IAlignmentNode alignmentNode) {
    nodes.add(alignmentNode);
  }

  private void addNewArc(IAlignmentNode start, IAlignmentNode end, IWitness witness) {
    IAlignmentArc arc = new AlignmentArc(start, end, witness);
    arcs.add(arc);
  }
}
