package eu.interedition.collatex2.experimental.graph;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class AlignmentGraph implements IAlignmentGraph {
  private final List<IAlignmentNode> nodes;

  public static IAlignmentGraph create(IWitness a) {
    final AlignmentGraph graph = new AlignmentGraph();
    for (INormalizedToken token : a.getTokens()) {
      graph.add(new AlignmentNode(token.getNormalized()));
    }
    return graph;
  }

  private void add(AlignmentNode alignmentNode) {
    nodes.add(alignmentNode);
  }

  private AlignmentGraph() {
    this.nodes = Lists.newArrayList();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * eu.interedition.collatex2.experimental.graph.IAlignmentGraph#getNodes()
   */
  public List<IAlignmentNode> getNodes() {
    return nodes;
  }
}
