package eu.interedition.collatex2.experimental.graph;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex2.implementation.indexing.NullToken;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class AlignmentGraph implements IAlignmentGraph {
  private final List<IAlignmentNode> nodes;
  private final IAlignmentNode       startNode;
  private final List<IAlignmentArc>  arcs;

  public static AlignmentGraph create() {
    final AlignmentGraph graph = new AlignmentGraph();
    return graph;
  }

  public static AlignmentGraph create(IWitness a) {
    AlignmentGraph graph = create();
    List<IAlignmentNode> newNodes = Lists.newArrayList();
    for (INormalizedToken token : a.getTokens()) {
      newNodes.add(graph.addNewNode(token));
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

  // TODO: why does NullToken need a sigil as parameter?
  private AlignmentGraph() {
    this.nodes = Lists.newArrayList();
    this.arcs = Lists.newArrayList();
    this.startNode = addNewNode(new NullToken(1, null));
  }

  private IAlignmentNode addNewNode(INormalizedToken token) {
    final AlignmentNode newNode = new AlignmentNode(token);
    nodes.add(newNode);
    return newNode;
  }

  private void addNewArc(IAlignmentNode start, IAlignmentNode end, IWitness witness) {
    IAlignmentArc arc = new AlignmentArc(start, end, witness);
    arcs.add(arc);
  }

  // TODO Auto-generated method stub
  @Override
  public List<String> findRepeatingTokens() {
    return Lists.newArrayList();
  }

  //NOTE: tokenA is the token from the Witness
  public void addWitness(IWitness witness) {
    AlignmentGraphWitnessMatcher matcher = new AlignmentGraphWitnessMatcher(this);
    List<ITokenMatch> matches = matcher.getMatches(witness);
    makeArcsForMatches(witness, matches, matcher.getGraphIndex());   
  }

  private void makeArcsForMatches(IWitness witness, List<ITokenMatch> matches, IAlignmentGraphIndex graphIndex2) {
    Map<INormalizedToken, ITokenMatch> witnessTokenToMatch;
    witnessTokenToMatch = Maps.newLinkedHashMap();
    for (ITokenMatch match : matches) {
      INormalizedToken tokenA = match.getTokenA();
      witnessTokenToMatch.put(tokenA, match);
    }
    IAlignmentNode begin = this.getStartNode();
    for (INormalizedToken token : witness.getTokens()) {
      if (!witnessTokenToMatch.containsKey(token)) {
        throw new RuntimeException("Token "+token+ " is not a match!");
      }
      //NOTE: it is a match!
      ITokenMatch tokenMatch = witnessTokenToMatch.get(token);
      IAlignmentNode end = graphIndex2.getAlignmentNode(tokenMatch.getTokenB());
      IAlignmentArc existingArc = find(begin, end);
      existingArc.getWitnesses().add(witness);
      begin = end;
    }
  }

  private IAlignmentArc find(IAlignmentNode begin, IAlignmentNode end) {
    for (IAlignmentArc arc: arcs) {
      if (arc.getBeginNode().equals(begin)&&arc.getEndNode().equals(end)) {
        return arc;
      }
    }
    throw new RuntimeException("Arc "+begin+ " "+end+" not found!");
  }
}
