package eu.interedition.collatex2.implementation.output.jgraph;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import eu.interedition.collatex2.interfaces.IJVariantGraph;
import eu.interedition.collatex2.interfaces.IJVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;

public class JVariantGraphCreator {
  private static final Logger LOG = LoggerFactory.getLogger(JVariantGraphCreator.class);
  private static IVariantGraph unjoinedGraph;
  private static IJVariantGraph joinedGraph;
  private static Map<IVariantGraphVertex, IJVariantGraphVertex> vertexMap;

  public static IJVariantGraph parallelSegmentate(final IVariantGraph _unjoinedGraph) {
    unjoinedGraph = _unjoinedGraph;
    //    LOG.info("edges: {}", unjoinedGraph.edgeSet().size());
    joinedGraph = JVariantGraph.create();
    vertexMap = Maps.newHashMap();

    IVariantGraphVertex startVgVertex = unjoinedGraph.getStartVertex();
    JVariantGraphVertex startJvgVertex = new JVariantGraphVertex(startVgVertex);
    joinedGraph.setStartVertex(startJvgVertex);
    vertexMap.put(startVgVertex, startJvgVertex);

    IVariantGraphVertex endVgVertex = unjoinedGraph.getEndVertex();
    JVariantGraphVertex endJvgVertex = new JVariantGraphVertex(endVgVertex);
    joinedGraph.setEndVertex(endJvgVertex);
    vertexMap.put(endVgVertex, endJvgVertex);

    Set<IVariantGraphEdge> outgoingEdges = unjoinedGraph.outgoingEdgesOf(startVgVertex);
    for (IVariantGraphEdge vgEdge : outgoingEdges) {
      processEdge(vgEdge, startJvgVertex);
    }

    return joinedGraph;
  }

  private static void processEdge(IVariantGraphEdge vgEdge, IJVariantGraphVertex lastJvgVertex) {
    //    LOG.info("edge: {} {}", vgEdge, vgEdge.hashCode());
    IVariantGraphVertex vgVertex = unjoinedGraph.getEdgeTarget(vgEdge);
    IJVariantGraphVertex jvgVertex;
    boolean vgVertexIsNew = true;
    if (vertexMap.containsKey(vgVertex)) {
      jvgVertex = vertexMap.get(vgVertex);
      vgVertexIsNew = false;
    } else {
      jvgVertex = new JVariantGraphVertex(vgVertex);
      vertexMap.put(vgVertex, jvgVertex);
      joinedGraph.addVertex(jvgVertex);
    }
    JVariantGraphEdge jvgEdge = new JVariantGraphEdge(lastJvgVertex, jvgVertex, vgEdge);
    joinedGraph.addEdge(lastJvgVertex, jvgVertex, jvgEdge);
    if (vgVertexIsNew) {
      checkNextVertex(vgVertex, jvgVertex);
    }
  }

  private static void checkNextVertex(IVariantGraphVertex vgVertex, IJVariantGraphVertex jvgVertex) {
    Set<IVariantGraphEdge> outgoingEdges = unjoinedGraph.outgoingEdgesOf(vgVertex);
    if (outgoingEdges.size() == 1) {
      IVariantGraphVertex targetVgVertex = unjoinedGraph.getEdgeTarget(outgoingEdges.iterator().next());
      if (vertexHasOneIncomingEdge(targetVgVertex) && vertexHasOutgoingEdges(targetVgVertex)) {
        if (!vertexMap.containsKey(targetVgVertex)) {
          jvgVertex.addVariantGraphVertex(targetVgVertex);
        }
        vertexMap.put(targetVgVertex, jvgVertex);
        checkNextVertex(targetVgVertex, jvgVertex);
      } else {
        processEdge(outgoingEdges.iterator().next(), jvgVertex);
      }
    } else {
      for (IVariantGraphEdge vgEdge : outgoingEdges) {
        processEdge(vgEdge, jvgVertex);
      }
    }
  }

  private static boolean vertexHasOneIncomingEdge(final IVariantGraphVertex vertex) {
    return unjoinedGraph.inDegreeOf(vertex) == 1;
  }

  private static boolean vertexHasOutgoingEdges(IVariantGraphVertex vertex) {
    return unjoinedGraph.outDegreeOf(vertex) > 0;
  }

}
