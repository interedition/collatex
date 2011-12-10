package eu.interedition.collatex.lab;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import eu.interedition.collatex.interfaces.IWitness;

import java.util.Map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraph extends DirectedSparseGraph<VariantGraphVertex, VariantGraphEdge> {

  private VariantGraphVertex start;
  private VariantGraphVertex end;

  public VariantGraphVertex getStart() {
    return start;
  }

  public void setStart(VariantGraphVertex start) {
    this.start = start;
  }

  public VariantGraphVertex getEnd() {
    return end;
  }

  public void setEnd(VariantGraphVertex end) {
    this.end = end;
  }

  public void update(eu.interedition.collatex.implementation.graph.db.VariantGraph pvg) {
    for (VariantGraphEdge edge : Lists.newArrayList(getEdges())) {
      removeEdge(edge);
    }
    for (VariantGraphVertex vertex : Lists.newArrayList(getVertices())) {
      removeVertex(vertex);
    }

    final Map<eu.interedition.collatex.implementation.graph.db.VariantGraphVertex, VariantGraphVertex> vertexMap = Maps.newHashMap();
    for (eu.interedition.collatex.implementation.graph.db.VariantGraphVertex pv : pvg.vertices()) {
      final VariantGraphVertex v = new VariantGraphVertex(pv.tokens(null), pv.getRank());
      addVertex(v);
      vertexMap.put(pv, v);
      if (pvg.getStart().equals(pv)) {
        setStart(v);
      } else if (pvg.getEnd().equals(pv)) {
        setEnd(v);
      }
    }
    for (eu.interedition.collatex.implementation.graph.db.VariantGraphEdge pe : pvg.edges()) {
      addEdge(new VariantGraphEdge(pe.getWitnesses()), vertexMap.get(pe.from()), vertexMap.get(pe.to()));
    }
    
    for (eu.interedition.collatex.implementation.graph.db.VariantGraphTransposition t : pvg.transpositions()) {
      addEdge(new VariantGraphEdge(Sets.<IWitness>newTreeSet()), vertexMap.get(t.getStart()), vertexMap.get(t.getEnd()));
    }
  }
}
