package eu.interedition.collatex2.experimental;

import java.util.Map;

import com.google.common.collect.Maps;

import eu.interedition.collatex2.implementation.containers.graph.VariantGraphEdge;
import eu.interedition.collatex2.implementation.containers.graph.VariantGraphVertex;
import eu.interedition.collatex2.implementation.vg_alignment.IAlignment2;
import eu.interedition.collatex2.implementation.vg_analysis.Analysis;
import eu.interedition.collatex2.implementation.vg_analysis.IAnalysis;
import eu.interedition.collatex2.interfaces.IAligner;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IWitness;

//TODO: rename to my new variant graph builder
//TODO: extract the real aligner out of this class
public class MyNewAligner implements IAligner {

  private final IVariantGraph graph;
  private Analysis analysis;

  public MyNewAligner(IVariantGraph graph) {
    this.graph = graph;
  }

  public void addWitness(IWitness witness) {
    Map<INormalizedToken, INormalizedToken> linkedTokens = alignWitnessTokensToTokensInGraph(witness);
    
    IVariantGraphVertex previous =  graph.getStartVertex();
    for (INormalizedToken token : witness.getTokens()) {
      // determine whether this token is a match or not
      // System.out.println(token+":"+linkedTokens.containsKey(token));
      IVariantGraphVertex vertex = linkedTokens.containsKey(token) ? (IVariantGraphVertex) linkedTokens.get(token) : addNewVertex(token.getNormalized(), token);
      IVariantGraphEdge edge = graph.getEdge(previous, vertex);
      if (edge == null) edge = addNewEdge(previous, vertex);
      vertex.addToken(witness, token);
      edge.addWitness(witness);
      previous = vertex;
    }
    IVariantGraphEdge edge = graph.getEdge(previous, graph.getEndVertex());
    if (edge == null) edge = addNewEdge(previous, graph.getEndVertex());
    edge.addWitness(witness);
  }

  private Map<INormalizedToken, INormalizedToken> alignWitnessTokensToTokensInGraph(IWitness witness) {
    if (graph.isEmpty()) {
      return Maps.newLinkedHashMap();
    } 
    SuperbaseCreator creator = new SuperbaseCreator();
    IWitness superbase = creator.create(graph);
    TheRealAligner aligner = new TheRealAligner();
    Map<INormalizedToken, INormalizedToken> alignedTokens;
    alignedTokens = aligner.align(superbase, witness);
    //NOTE: This is not very nice!
    this.analysis = aligner.getAnalysis();
    return alignedTokens;
  }

  //write
  private IVariantGraphVertex addNewVertex(String normalized, INormalizedToken vertexKey) {
    // System.out.println("Add vertex "+normalized);
    IVariantGraphVertex vertex = new VariantGraphVertex(normalized, vertexKey);
    graph.addVertex(vertex);
    return vertex;
  }

  //write
  private IVariantGraphEdge addNewEdge(IVariantGraphVertex begin, IVariantGraphVertex end) {
    // System.out.println("Add edge between "+begin.getNormalized()+ " and " + end.getNormalized());
    IVariantGraphEdge edge = new VariantGraphEdge();
    graph.addEdge(begin, end, edge);
    return edge;
  }

  @Override
  public IVariantGraph getResult() {
    return graph;
  }

  @Override
  public IAligner add(IWitness... witnesses) {
    for (IWitness witness : witnesses) {
      addWitness(witness);
    }
    return this;
  }


  @Override
  public IAlignment2 align(IWitness witness) {
    throw new RuntimeException("NOT YET IMPLEMENTED!");
  }

  public IAnalysis getAnalysis() {
    return analysis;
  }
}
