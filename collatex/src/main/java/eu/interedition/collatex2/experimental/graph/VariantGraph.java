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

public class VariantGraph extends BaseDirectedGraph implements IVariantGraph {
  private final IVariantGraphVertex startVertex;
  private final IVariantGraphVertex endVertex;
  private final List<IWitness> witnesses;

  // read
  @Override
  public IVariantGraphVertex getStartVertex() {
    return startVertex;
  }

  // read
  @Override
  public IVariantGraphVertex getEndVertex() {
    return endVertex;
  }

  //TODO: remove method -> use edgeSet!
  // read
  @Override
  public List<IVariantGraphEdge> getEdges() {
    return Lists.newArrayList(edgeSet());
  }

  // constructor
  // TODO: why does NullToken need a sigil as parameter?
  protected VariantGraph() {
    this.startVertex = new VariantGraphVertex(new NullToken(1, null));
    this.endVertex = new VariantGraphVertex(new NullToken(1, null));
    this.witnesses = Lists.newArrayList();
  }

  // read (for indexing)
  // TODO Auto-generated method stub
  @Override
  public List<String> findRepeatingTokens() {
    return Lists.newArrayList();
  }

  // read
  @Override
  public boolean isEmpty() {
    return getWitnesses().isEmpty();
  }

  // read
  //TODO: rename!
  public List<IVariantGraphEdge> getArcsForWitness(IWitness witness) {
    IVariantGraphVertex vertex = getStartVertex();
    List<IVariantGraphEdge> edges = Lists.newArrayList();
    while (hasEdge(vertex, witness)) {
      final IVariantGraphEdge edge = findEdge(vertex, witness);
      edges.add(edge);
      vertex = edge.getEndVertex();
    }
    return edges;
  }

  //TODO: remove this method!
  //use getArcsForWitness instead!
  // read
  public List<IVariantGraphVertex> getPath(IWitness witness) {
    IVariantGraphVertex beginNode = getStartVertex();
    List<IVariantGraphVertex> path = Lists.newArrayList();
    while (hasEdge(beginNode, witness)) {
      IVariantGraphEdge arc = findEdge(beginNode, witness);
      IVariantGraphVertex endNode = arc.getEndVertex();
      path.add(endNode);
      beginNode = endNode;
    }
    return path;
  }

  

  //read
  @Override
  public List<IVariantGraphVertex> getVertices() {
    List<IVariantGraphVertex> allVertices = Lists.newArrayList();
    allVertices.add(getStartVertex());
    allVertices.addAll(vertexSet());
    allVertices.add(getEndVertex());
    return allVertices;
  }

  //read
  @Override
  public List<IWitness> getWitnesses() {
    return witnesses;
  }

  //write
  public static VariantGraph create() {
    final VariantGraph graph = new VariantGraph();
    return graph;
  }

  //write
  public static VariantGraph create(IWitness a) {
    VariantGraph graph = create();
    graph.getWitnesses().add(a);
    List<IVariantGraphVertex> newVertices = Lists.newArrayList();
    for (INormalizedToken token : a.getTokens()) {
      newVertices.add(graph.addNewVertex(token, a));
    }
    IVariantGraphVertex previous = graph.getStartVertex();
    for (IVariantGraphVertex vertex : newVertices) {
      graph.addNewEdge(previous, vertex, a);
      previous = vertex;
    }
    graph.addNewEdge(previous, graph.getEndVertex(), a);
    return graph;
  }
  
  //write
  public IVariantGraphVertex addNewVertex(INormalizedToken token, IWitness w) {
    final VariantGraphVertex vertex = new VariantGraphVertex(token);
    addVertex(vertex);
    if (w!=null) {
      vertex.addToken(w, token);
    }
    return vertex;
  }

  // write
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

  //write
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
        addNewEdge(begin, end, witness);
        begin = end;
      } else {
        // NOTE: it is a match!
        ITokenMatch tokenMatch = witnessTokenToMatch.get(token);
        IVariantGraphVertex end = graphIndex2.getVertex(tokenMatch.getTokenB());
        connectBeginToEndVertex(begin, end, witness);
        end.addToken(witness, token);
        begin = end;
      }
    }
    // adds edge from last vertex to end vertex
    IVariantGraphVertex end = getEndVertex();
    connectBeginToEndVertex(begin, end, witness);
  }

  private void connectBeginToEndVertex(IVariantGraphVertex begin, IVariantGraphVertex end, IWitness witness) {
    if (hasEdge(begin, end)) {
      IVariantGraphEdge existingEdge = findEdge(begin, end);
      existingEdge.addWitness(witness);
    } else {
      addNewEdge(begin, end, witness);
    }
  }

  //TODO: inline method!
  private IVariantGraphEdge findEdge(IVariantGraphVertex begin, IVariantGraphVertex end) {
    return super.getEdge(begin, end);    
  }

  //TODO: inline method!
  private boolean hasEdge(IVariantGraphVertex begin, IVariantGraphVertex end) {
    return super.containsEdge(begin, end);
  }

  private void addNewEdge(IVariantGraphVertex begin, IVariantGraphVertex end, IWitness witness) {
    IVariantGraphEdge e = new VariantGraphEdge(begin, end, witness);
    super.addEdge(begin, end, e);
  }
  
  public IVariantGraphEdge findEdge(IVariantGraphVertex source, IWitness witness) {
    for (IVariantGraphEdge edge : edgeSet()) {
      if (edge.getBeginVertex().equals(source) && edge.getWitnesses().contains(witness)) {
        return edge;
      }
    }
    return null;
  }
  
  public boolean hasEdge(IVariantGraphVertex source, IWitness witness) {
    return findEdge(source, witness) != null;
  }



}
