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
  private final List<IVariantGraphNode> nodes;
  private final IVariantGraphNode       startNode;
  private final List<IVariantGraphArc>  arcs;
  private final List<IWitness> witnesses; 

  public static VariantGraph create() {
    final VariantGraph graph = new VariantGraph();
    return graph;
  }

  public static VariantGraph create(IWitness a) {
    VariantGraph graph = create();
    graph.getWitnesses().add(a);
    List<IVariantGraphNode> newNodes = Lists.newArrayList();
    for (INormalizedToken token : a.getTokens()) {
      newNodes.add(graph.addNewNode(token));
    }
    IVariantGraphNode previous = graph.getStartNode();
    for (IVariantGraphNode node : newNodes) {
      graph.addNewArc(previous, node, a);
      previous = node;
    }
    return graph;
  }

  @Override
  public List<IVariantGraphNode> getNodes() {
    return nodes;
  }

  @Override
  public IVariantGraphNode getStartNode() {
    return startNode;
  }

  @Override
  public List<IVariantGraphArc> getArcs() {
    return arcs;
  }

  // TODO: why does NullToken need a sigil as parameter?
  private VariantGraph() {
    this.nodes = Lists.newArrayList();
    this.arcs = Lists.newArrayList();
    this.witnesses = Lists.newArrayList();
    this.startNode = addNewNode(new NullToken(1, null));
  }

  private IVariantGraphNode addNewNode(INormalizedToken token) {
    final VariantGraphNode newNode = new VariantGraphNode(token);
    nodes.add(newNode);
    return newNode;
  }

  private void addNewArc(IVariantGraphNode start, IVariantGraphNode end, IWitness witness) {
    IVariantGraphArc arc = new VariantGraphArc(start, end, witness);
    arcs.add(arc);
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
    IVariantGraphNode begin = this.getStartNode();
    for (INormalizedToken token : witness.getTokens()) {
      if (!witnessTokenToMatch.containsKey(token)) {
        // NOTE: here we determine that the token is an addition/replacement!
        IVariantGraphNode end = this.addNewNode(token);
        this.addNewArc(begin, end, witness);
        begin = end;
      } else {
        // NOTE: it is a match!
        ITokenMatch tokenMatch = witnessTokenToMatch.get(token);
        IVariantGraphNode end = graphIndex2.getAlignmentNode(tokenMatch.getTokenB());
        if (this.arcExist(begin, end)) {
          IVariantGraphArc existingArc = find(begin, end);
          existingArc.getWitnesses().add(witness);
        } else {
          this.addNewArc(begin, end, witness);
        }
        begin = end;
      }
    }
  }

  private boolean arcExist(IVariantGraphNode begin, IVariantGraphNode end) {
    for (IVariantGraphArc arc : arcs) {
      if (arc.getBeginNode().equals(begin) && arc.getEndNode().equals(end)) {
        return true;
      }
    }
    return false;
  }

  private IVariantGraphArc find(IVariantGraphNode begin, IVariantGraphNode end) {
    for (IVariantGraphArc arc : arcs) {
      if (arc.getBeginNode().equals(begin) && arc.getEndNode().equals(end)) {
        return arc;
      }
    }
    throw new RuntimeException("Arc '" + begin.getNormalized() + "' -> '" + end.getNormalized() + "' not found!");
  }

  @Override
  public List<IWitness> getWitnesses() {
    return witnesses;
  }

  @Override
  public boolean isEmpty() {
    return witnesses.isEmpty();
  }

  @Override
  public IVariantGraphArc findArc(IVariantGraphNode begin, IWitness witness) {
    for (IVariantGraphArc arc : arcs) {
      if (arc.getBeginNode().equals(begin) && arc.getWitnesses().contains(witness)) {
        return arc;
      }
    }
    return null;
  }

  @Override
  public boolean hasArc(IVariantGraphNode beginNode, IWitness witness) {
    return findArc(beginNode, witness) != null;
  }
  
  public List<IVariantGraphNode> getPath(IWitness witness) {
    IVariantGraphNode beginNode = getStartNode();
    List<IVariantGraphNode> path = Lists.newArrayList();
    while (hasArc(beginNode, witness)) {
      IVariantGraphArc arc = findArc(beginNode, witness);
      IVariantGraphNode endNode = arc.getEndNode();
      path.add(endNode);
      beginNode = endNode;
    }
    return path;
  }

}
