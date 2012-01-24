package eu.interedition.collatex.lab;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import eu.interedition.collatex.graph.EditGraph;
import eu.interedition.collatex.graph.EditGraphEdge;
import eu.interedition.collatex.graph.EditGraphVertex;

import java.util.Map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class EditGraphModel extends DirectedSparseGraph<EditGraphVertexModel, EditGraphEdgeModel> {

  private EditGraphVertexModel start;
  private EditGraphVertexModel end;

  public EditGraphVertexModel getStart() {
    return start;
  }

  public void setStart(EditGraphVertexModel start) {
    this.start = start;
  }

  public EditGraphVertexModel getEnd() {
    return end;
  }

  public void setEnd(EditGraphVertexModel end) {
    this.end = end;
  }

  public void update(EditGraph eg) {
    for (EditGraphEdgeModel edgeModel : Lists.newArrayList(getEdges())) {
      removeEdge(edgeModel);
    }
    for (EditGraphVertexModel vertexModel : Lists.newArrayList(getVertices())) {
      removeVertex(vertexModel);
    }

    final Map<EditGraphVertex, EditGraphVertexModel> vertexMap = Maps.newHashMap();
    for (EditGraphVertex ev : eg.vertices()) {
      final EditGraphVertexModel v = new EditGraphVertexModel(ev.getWitness(), ev.getBase().tokens());
      addVertex(v);
      vertexMap.put(ev, v);
      if (eg.getStart().equals(ev)) {
        setStart(v);
      } else if (eg.getEnd().equals(ev)) {
        setEnd(v);
      }
    }
    for (EditGraphEdge ee : eg.edges()) {
      addEdge(new EditGraphEdgeModel(ee.getEditOperation(), ee.getShortestPathIds(), ee.getScore()), vertexMap.get(ee.from()), vertexMap.get(ee.to()));
    }

  }
}
