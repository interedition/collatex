package eu.interedition.collatex2.implementation.output.rankedgraph;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex2.implementation.output.segmented_graph.ISegmentedVariantGraph;
import eu.interedition.collatex2.implementation.output.segmented_graph.ISegmentedVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IVariantGraphEdge;

public class VariantGraphRanker {

  private final ISegmentedVariantGraph graph;

  public VariantGraphRanker(ISegmentedVariantGraph graph) {
    this.graph = graph;
  }

  public Iterator<IRankedVariantGraphVertex> iterator() {
    final Map<ISegmentedVariantGraphVertex, Integer> vertexToRankMap = Maps.newLinkedHashMap();
    final Iterator<ISegmentedVariantGraphVertex> iterator = graph.iterator();
    ISegmentedVariantGraphVertex startVertex = iterator.next();
    vertexToRankMap.put(startVertex, 0);
    return new Iterator<IRankedVariantGraphVertex>() {

      //TODO: skip end vertex?
      @Override
      public boolean hasNext() {
        return iterator.hasNext();
      }

      @Override
      public IRankedVariantGraphVertex next() {
        ISegmentedVariantGraphVertex vertex = iterator.next();
        Set<IVariantGraphEdge> incomingEdges = graph.incomingEdgesOf(vertex);
        int maxRankParent = -1;
        for (IVariantGraphEdge edgeFromParent : incomingEdges) {
          ISegmentedVariantGraphVertex parent = graph.getEdgeSource(edgeFromParent);
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
