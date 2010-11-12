package eu.interedition.collatex2.implementation.output.cgraph;

import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.Maps;

import eu.interedition.collatex2.implementation.containers.graph.VariantGraphEdge;
import eu.interedition.collatex2.implementation.containers.graph.VariantGraphVertex;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IWitness;

public class CVariantGraphCreator {
  //  private static final Logger LOG = LoggerFactory.getLogger(CVariantGraphCreator.class);
  private static IVariantGraph cyclicGraph;
  private static Map<INormalizedToken, IVariantGraphVertex> keyToken2Vertex;
  private static Map<IVariantGraphVertex, IVariantGraphVertex> a2cVertexMap;

  public static IVariantGraph getCyclicVariantGraph(final IVariantGraph acyclicGraph) {
    keyToken2Vertex = Maps.newHashMap();
    a2cVertexMap = Maps.newHashMap(); // maps vertex in acyclic graph to vertex in cyclic graph
    cyclicGraph = CyclicVariantGraph.create();

    a2cVertexMap.put(acyclicGraph.getStartVertex(), cyclicGraph.getStartVertex());
    a2cVertexMap.put(acyclicGraph.getEndVertex(), cyclicGraph.getEndVertex());

    for (IVariantGraphVertex avgVertex : acyclicGraph.vertexSet()) {
      if (!a2cVertexMap.containsKey(avgVertex)) {
        INormalizedToken vertexKey = avgVertex.getVertexKey();
        IVariantGraphVertex cvgVertex;
        if (keyToken2Vertex.containsKey(vertexKey)) {
          cvgVertex = keyToken2Vertex.get(vertexKey);
        } else {
          cvgVertex = new VariantGraphVertex(avgVertex.getNormalized(), vertexKey);
          keyToken2Vertex.put(vertexKey, cvgVertex);
          cyclicGraph.addVertex(cvgVertex);
        }
        a2cVertexMap.put(avgVertex, cvgVertex);
      }
    }

    for (IVariantGraphEdge avgEdge : acyclicGraph.edgeSet()) {
      IVariantGraphVertex cvgStart = a2cVertexMap.get(avgEdge.getBeginVertex());
      IVariantGraphVertex cvgEnd = a2cVertexMap.get(avgEdge.getEndVertex());

      Iterator<IWitness> witnessIterator = avgEdge.getWitnesses().iterator();
      IVariantGraphEdge cvgEdge;
      if (cyclicGraph.containsEdge(cvgStart, cvgEnd)) {
        cvgEdge = cyclicGraph.getEdge(cvgStart, cvgEnd);
      } else {
        cvgEdge = new VariantGraphEdge(cvgStart, cvgEnd, witnessIterator.next());
        cyclicGraph.addEdge(cvgStart, cvgEnd, cvgEdge);
      }
      while (witnessIterator.hasNext()) {
        cvgEdge.addWitness(witnessIterator.next());
      }
    }

    return cyclicGraph;
  }

}
