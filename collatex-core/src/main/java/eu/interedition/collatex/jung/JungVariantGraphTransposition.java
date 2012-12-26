package eu.interedition.collatex.jung;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import eu.interedition.collatex.VariantGraph;

import java.util.Iterator;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class JungVariantGraphTransposition implements VariantGraph.Transposition {

  private final JungVariantGraph graph;
  private final Set<JungVariantGraphVertex> vertices;

  public JungVariantGraphTransposition(JungVariantGraph graph, Set<VariantGraph.Vertex> vertices) {
    this.graph = graph;
    this.vertices = Sets.newHashSet(Iterables.filter(vertices, JungVariantGraphVertex.class));
    for (JungVariantGraphVertex vertex : this.vertices) {
      graph.transpositionIndex.put(vertex, this);
    }
  }

  @Override
  public void delete() {
    for (JungVariantGraphVertex vertex : this.vertices) {
      graph.transpositionIndex.remove(vertex, this);
    }
  }

  @Override
  public Iterator<VariantGraph.Vertex> iterator() {
    return Iterators.filter(vertices.iterator(), VariantGraph.Vertex.class);
  }

  @Override
  public String toString() {
    return Iterables.toString(vertices);
  }
}
