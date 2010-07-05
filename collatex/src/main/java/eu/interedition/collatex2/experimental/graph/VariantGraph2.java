package eu.interedition.collatex2.experimental.graph;

import java.util.List;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.experimental.graph.IVariantGraph;
import eu.interedition.collatex2.experimental.graph.IVariantGraphEdge;
import eu.interedition.collatex2.experimental.graph.IVariantGraphVertex;
import eu.interedition.collatex2.experimental.graph.VariantGraphVertex;
import eu.interedition.collatex2.implementation.indexing.NullToken;
import eu.interedition.collatex2.interfaces.IWitness;

// This class implements the IVariantGraph interface.
// The IVariantGraph interface is an extension of the DiGraph interface
// The implementation is based on a DAG.
// The VariantGraph contains a start and an end vertex.
// The VariantGraph contains a List of witnesses that have
// been added to the Graph.
@SuppressWarnings("serial")
public class VariantGraph2 extends DirectedAcyclicGraph<IVariantGraphVertex, IVariantGraphEdge> implements IVariantGraph {
  private final IVariantGraphVertex startVertex;
  private final IVariantGraphVertex endVertex;
  private final List<IWitness> witnesses;

  private VariantGraph2() {
    super(IVariantGraphEdge.class);
    this.witnesses = Lists.newArrayList();
    startVertex = new VariantGraphVertex(new NullToken(0, null));
    addVertex(startVertex);
    endVertex = new VariantGraphVertex(new NullToken(0, null));
    addVertex(endVertex);
  }

  @Override
  public void addWitness(IWitness a) {
    throw new RuntimeException("!!");
  }

  //TODO: implement!
  @Override
  public List<String> findRepeatingTokens() {
    throw new RuntimeException("!!");
  }

  //TODO: remove!
  @Override
  public List<IVariantGraphEdge> getEdges() {
    throw new RuntimeException("!!");
  }

  @Override
  public IVariantGraphVertex getEndVertex() {
    return endVertex;
  }

  @Override
  public List<IVariantGraphVertex> getPath(IWitness witness) {
    throw new RuntimeException("!!");
  }

  @Override
  public IVariantGraphVertex getStartVertex() {
    return startVertex;
  }

  @Override
  public List<IWitness> getWitnesses() {
    return witnesses;
  }

  @Override
  public boolean isEmpty() {
    return witnesses.isEmpty();
  }

  public static IVariantGraph create() {
    return new VariantGraph2();
  }

}
