package eu.interedition.collatex2.experimental.graph;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex2.experimental.graph.indexing.IVariantGraphIndex;
import eu.interedition.collatex2.experimental.graph.indexing.VariantGraphIndexMatcher;
import eu.interedition.collatex2.implementation.indexing.NullToken;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.ITokenMatch;
import eu.interedition.collatex2.interfaces.IWitness;

public class VariantGraph implements IVariantGraph {
  private final List<IVariantGraphVertex> vertices;
  private final List<IWitness>          witnesses;
  private final IVariantGraphVertex       startVertex;
  private final IVariantGraphVertex endVertex;

  public static VariantGraph create() {
    final VariantGraph graph = new VariantGraph();
    return graph;
  }

  public static VariantGraph create(IWitness a) {
    VariantGraph graph = create();
    graph.getWitnesses().add(a);
    List<IVariantGraphVertex> newVertices = Lists.newArrayList();
    for (INormalizedToken token : a.getTokens()) {
      newVertices.add(graph.addNewVertex(token, a));
    }
    IVariantGraphVertex previous = graph.getStartVertex();
    for (IVariantGraphVertex vertex : newVertices) {
      previous.addNewEdge(vertex, a);
      previous = vertex;
    }
    previous.addNewEdge(graph.getEndVertex(), a);
    return graph;
  }

  @Override
  public List<IVariantGraphVertex> getVertices() {
    List<IVariantGraphVertex> allVertices = Lists.newArrayList();
    allVertices.add(getStartVertex());
    allVertices.addAll(vertices);
    allVertices.add(getEndVertex());
    return allVertices;
  }

  @Override
  public IVariantGraphVertex getStartVertex() {
    return startVertex;
  }

  @Override
  public IVariantGraphVertex getEndVertex() {
    return endVertex;
  }

  // TODO: why does NullToken need a sigil as parameter?
  private VariantGraph() {
    this.vertices = Lists.newArrayList();
    this.witnesses = Lists.newArrayList();
    this.startVertex = new VariantGraphVertex(new NullToken(1, null)); 
    this.endVertex = new VariantGraphVertex(new NullToken(1, null));
  }

  private IVariantGraphVertex addNewVertex(INormalizedToken token, IWitness w) {
    final VariantGraphVertex vertex = new VariantGraphVertex(token);
    vertices.add(vertex);
    if (w!=null) {
      vertex.addToken(w, token);
    }
    return vertex;
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
    makeEdgesForMatches(witness, matches, matcher.getGraphIndex());
  }

  //TODO: move building of graph to subclass?
  private void makeEdgesForMatches(IWitness witness, List<ITokenMatch> matches, IVariantGraphIndex graphIndex2) {
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
        IVariantGraphVertex end = this.addNewVertex(token, witness);
        begin.addNewEdge(end, witness);
        begin = end;
      } else {
        // NOTE: it is a match!
        ITokenMatch tokenMatch = witnessTokenToMatch.get(token);
        IVariantGraphVertex end = graphIndex2.getVertex(tokenMatch.getTokenB());
        if (begin.hasEdge(end)) {
          IVariantGraphEdge existingEdge = begin.findEdge(end);
          existingEdge.addWitness(witness);
          end.addToken(witness, token);
        } else {
          begin.addNewEdge(end, witness);
          end.addToken(witness, token);
        }
        begin = end;
      }
    }
    // adds edge from last vertex to end vertex
    IVariantGraphVertex end = getEndVertex();
    if (begin.hasEdge(end)) {
      IVariantGraphEdge existingEdge = begin.findEdge(end);
      existingEdge.addWitness(witness);
    } else {
      begin.addNewEdge(end, witness);
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
    for (IVariantGraphVertex node : getVertices()) {
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
