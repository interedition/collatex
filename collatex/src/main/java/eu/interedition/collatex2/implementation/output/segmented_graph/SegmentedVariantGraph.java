package eu.interedition.collatex2.implementation.output.segmented_graph;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

import eu.interedition.collatex2.implementation.containers.graph.VariantGraphEdge;
import eu.interedition.collatex2.interfaces.IVariantGraphEdge;


/*************
 * 
 * @author ronald
 * This class is intended to be like a VariantGraph, only the difference is that parallel segmentation
 * is applied
 * So that VariantGraphVertices contain Phrases instead of Tokens!
 * This class has a strong relation with JGraph
 * It might be a good idea to merge the two in the future!
 */
@SuppressWarnings("serial")
public class SegmentedVariantGraph extends DirectedAcyclicGraph<ISegmentedVariantGraphVertex, IVariantGraphEdge> implements ISegmentedVariantGraph {
    private ISegmentedVariantGraphVertex endVertex;
    
	public SegmentedVariantGraph() {
		super(VariantGraphEdge.class);
	}

  @Override
  public ISegmentedVariantGraphVertex getEndVertex() {
    return endVertex;
  }

  protected void setEndVertex(ISegmentedVariantGraphVertex endVertex) {
    this.endVertex = endVertex;
  }

}
