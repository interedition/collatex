package eu.interedition.collatex2.implementation.output.rankedgraph;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;

public class VariantGraphRanker {

  private final IVariantGraph graph;

  public VariantGraphRanker(IVariantGraph graph) {
    this.graph = graph;
  }

  public Iterator<IRankedVariantGraphVertex> iterator() {
    final Map<IVariantGraphVertex, Integer> vertexToRankMap = Maps.newLinkedHashMap();
    final Iterator<IVariantGraphVertex> iterator = graph.iterator();
    IVariantGraphVertex startVertex = iterator.next();
    vertexToRankMap.put(startVertex, 0);
    return new Iterator<IRankedVariantGraphVertex>() {

      //TODO: skip end vertex?
      @Override
      public boolean hasNext() {
        return iterator.hasNext();
      }

      @Override
      public IRankedVariantGraphVertex next() {
        IVariantGraphVertex vertex = iterator.next();
        Set<IVariantGraphEdge> incomingEdges = graph.incomingEdgesOf(vertex);
        int maxRankParent = -1;
        for (IVariantGraphEdge edgeFromParent : incomingEdges) {
          IVariantGraphVertex parent = edgeFromParent.getBeginVertex();
          maxRankParent = Math.max(maxRankParent, vertexToRankMap.get(parent));
        }
        int rank = maxRankParent+1;
        vertexToRankMap.put(vertex, rank);
        return new RankedVariantGraphVertex(rank, vertex.getNormalized());
      }

      @Override
      public void remove() {
        iterator.remove();
      }};
  }

  public List<IRankedVariantGraphVertex> getRankedVertices() {
    return Lists.newArrayList(iterator());
  }

}
