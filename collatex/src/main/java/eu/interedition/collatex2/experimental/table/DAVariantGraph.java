package eu.interedition.collatex2.experimental.table;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

@SuppressWarnings("serial")
public class DAVariantGraph extends DirectedAcyclicGraph<CollateXVertex, CollateXEdge>{

  public DAVariantGraph(Class<? extends CollateXEdge> arg0) {
    super(arg0);
  }

}
