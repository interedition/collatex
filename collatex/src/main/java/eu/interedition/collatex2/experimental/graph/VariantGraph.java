package eu.interedition.collatex2.experimental.graph;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex2.implementation.indexing.NullToken;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.ITokenMatch;
import eu.interedition.collatex2.interfaces.IWitness;

public class VariantGraph implements IVariantGraph {
  private final List<IVariantGraphVertex> nodes;
  private final IVariantGraphVertex       startNode;
  private final List<IWitness>          witnesses;

  public static VariantGraph create() {
    final VariantGraph graph = new VariantGraph();
    return graph;
  }

  public static VariantGraph create(IWitness a) {
    VariantGraph graph = create();
    graph.getWitnesses().add(a);
    List<IVariantGraphVertex> newNodes = Lists.newArrayList();
    for (INormalizedToken token : a.getTokens()) {
      newNodes.add(graph.addNewNode(token));
    }
    IVariantGraphVertex previous = graph.getStartVertex();
    for (IVariantGraphVertex node : newNodes) {
      previous.addNewEdge(node, a, node.getToken());
      previous = node;
    }
    return graph;
  }

  @Override
  public List<IVariantGraphVertex> getVertices() {
    return nodes;
  }

  @Override
  public IVariantGraphVertex getStartVertex() {
    return startNode;
  }

  // TODO: why does NullToken need a sigil as parameter?
  private VariantGraph() {
    this.nodes = Lists.newArrayList();
    this.witnesses = Lists.newArrayList();
    this.startNode = addNewNode(new NullToken(1, null));
  }

  private IVariantGraphVertex addNewNode(INormalizedToken token) {
    final VariantGraphVertex newNode = new VariantGraphVertex(token);
    nodes.add(newNode);
    return newNode;
  }

  // TODO Auto-generated method stub
  @Override
  public List<String> findRepeatingTokens() {
    return Lists.newArrayList();
  }

  // NOTE: tokenA is the token from the Witness
  // For every token in the witness we have to map a VariantNode
  // for matches such a node should already exist
  // however for additions and replacements this will not be the case
  // then we need to add the arcs
  // in some cases the arcs may already exist
  // if they already exist we need to add the witness to the
  // existing arc!
  public void addWitness(IWitness witness) {
    witnesses.add(witness);
    VariantGraphIndexMatcher matcher = new VariantGraphIndexMatcher(this);
    List<ITokenMatch> matches = matcher.getMatches(witness);
    makeArcsForMatches(witness, matches, matcher.getGraphIndex());
  }

  private void makeArcsForMatches(IWitness witness, List<ITokenMatch> matches, IVariantGraphIndex graphIndex2) {
    Map<INormalizedToken, ITokenMatch> witnessTokenToMatch;
    witnessTokenToMatch = Maps.newLinkedHashMap();
    for (ITokenMatch match : matches) {
      INormalizedToken tokenA = match.getTokenA();
      witnessTokenToMatch.put(tokenA, match);
    }
    IVariantGraphVertex begin = this.getStartVertex();
    for (INormalizedToken token : witness.getTokens()) {
      if (!witnessTokenToMatch.containsKey(token)) {
        // NOTE: here we determine that the token is an addition/replacement!
        IVariantGraphVertex end = this.addNewNode(token);
        begin.addNewEdge(end, witness, token);
        begin = end;
      } else {
        // NOTE: it is a match!
        ITokenMatch tokenMatch = witnessTokenToMatch.get(token);
        IVariantGraphVertex end = graphIndex2.getAlignmentNode(tokenMatch.getTokenB());
        if (begin.hasEdge(end)) {
          IVariantGraphEdge existingArc = begin.findEdge(end);
          existingArc.addToken(witness, token);
        } else {
          begin.addNewEdge(end, witness, token);
        }
        begin = end;
      }
    }
  }

  @Override
  public List<IWitness> getWitnesses() {
    return witnesses;
  }

  @Override
  public boolean isEmpty() {
    return witnesses.isEmpty();
  }

  public List<IVariantGraphVertex> getPath(IWitness witness) {
    IVariantGraphVertex beginNode = getStartVertex();
    List<IVariantGraphVertex> path = Lists.newArrayList();
    while (beginNode.hasEdge(witness)) {
      IVariantGraphEdge arc = beginNode.findEdge(witness);
      IVariantGraphVertex endNode = arc.getEndVertex();
      path.add(endNode);
      beginNode = endNode;
    }
    return path;
  }

  // NOTE: only for testing purposes!
  @Override
  public List<IVariantGraphEdge> getEdges() {
    List<IVariantGraphEdge> allArcs = Lists.newArrayList();
    for (IVariantGraphVertex node : nodes) {
      allArcs.addAll(node.getEdges());
    }
    return allArcs;
  }

  public List<IVariantGraphEdge> getArcsForWitness(IWitness witness) {
    IVariantGraphVertex node = getStartVertex();
    List<IVariantGraphEdge> arcs = Lists.newArrayList();
    while (node.hasEdge(witness)) {
      final IVariantGraphEdge arc = node.findEdge(witness);
      arcs.add(arc);
      node = arc.getEndVertex();
    }
    return arcs;
  }
}
