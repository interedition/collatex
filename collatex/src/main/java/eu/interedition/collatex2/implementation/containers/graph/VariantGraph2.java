package eu.interedition.collatex2.implementation.containers.graph;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IToken;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
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

  public VariantGraph2() {
    super(IVariantGraphEdge.class);
    startVertex = new VariantGraphVertex("#", null);
    addVertex(startVertex);
    endVertex = new VariantGraphVertex("#", null);
    addVertex(endVertex);
  }

  @Override
  public IVariantGraphVertex getEndVertex() {
    return endVertex;
  }

  @Override
  public IVariantGraphVertex getStartVertex() {
    return startVertex;
  }

  @Override
  public List<IWitness> getWitnesses() {
    Set<IVariantGraphEdge> outgoingEdges = outgoingEdgesOf(startVertex);
    List<IWitness> totalWitnesses = Lists.newArrayList();
    for (IVariantGraphEdge edge : outgoingEdges) {
      totalWitnesses.addAll(edge.getWitnesses());
    }
    //NOTE: The set of outgoingEdges is unordered!
    //NOTE: That is unexpected behavior so the list of witnesses
    //NOTE: is sorted here! WOULD HAVE: insert order
    Collections.sort(totalWitnesses, new Comparator<IWitness>() {
      @Override
      public int compare(IWitness arg0, IWitness arg1) {
        return arg0.getSigil().compareTo(arg1.getSigil());
      } });
    return Collections.unmodifiableList(totalWitnesses);
  }

  @Override
  public boolean isEmpty() {
    return getWitnesses().isEmpty();
  }

  @Override
  public List<IVariantGraphEdge> getPath(IWitness witness) {
    List<IVariantGraphEdge> path = Lists.newArrayList();
    IVariantGraphVertex currentVertex = getStartVertex();
    while (currentVertex != getEndVertex()) {
      Set<IVariantGraphEdge> outgoingEdges = outgoingEdgesOf(currentVertex);
      boolean found = false;
      for (IVariantGraphEdge edge : outgoingEdges) {
        if (!found && edge.containsWitness(witness)) {
          found = true;
          path.add(edge);
          currentVertex = getEdgeTarget(edge);
          continue;
        }
      }
      if (!found) {
        throw new RuntimeException("No valid path found for " + witness.getSigil());
      }
    }
    return path;
  }



  @Override
  public List<INormalizedToken> getTokens(IWitness witness) {
    List<IVariantGraphEdge> edges = getPath(witness);
    List<INormalizedToken> tokens = Lists.newArrayList();
    for (IVariantGraphEdge edge : edges) {
      IVariantGraphVertex vertex = getEdgeTarget(edge);
      if (vertex != getEndVertex()) {
        tokens.add(vertex);
      }  
    }
    return tokens;
  }

  @Override
  public boolean isNear(IToken a, IToken b) {
    // sanity check!
    if (!(a instanceof IVariantGraphVertex)) {
      throw new RuntimeException("IToken a is not of type IVariantGraphVertex!");
    }
    if (!(b instanceof IVariantGraphVertex)) {
      throw new RuntimeException("IToken b is not of type IVariantGraphVertex!");
    }
    //NOTE: Vertices A and B should be connected and the maximum distance should be 1
    boolean connected = containsEdge((IVariantGraphVertex) a, (IVariantGraphVertex) b);
    connected = connected && (outDegreeOf((IVariantGraphVertex)a) == 1 || inDegreeOf((IVariantGraphVertex)b) == 1);
    return connected;
  }

  @Override
  public Iterator<INormalizedToken> tokenIterator() {
    final Iterator<IVariantGraphVertex> verticesIterator = iterator();
    return new Iterator<INormalizedToken>() {
      @Override
      public boolean hasNext() {
        return verticesIterator.hasNext();
      }

      @Override
      public INormalizedToken next() {
        IVariantGraphVertex vertex = verticesIterator.next();
        return vertex;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }
}
