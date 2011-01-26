package eu.interedition.collatex2.implementation.output.segmented_graph;

import java.util.Iterator;

import org.jgrapht.DirectedGraph;

import eu.interedition.collatex2.interfaces.IVariantGraphEdge;

public interface ISegmentedVariantGraph extends DirectedGraph<ISegmentedVariantGraphVertex, IVariantGraphEdge> {

  Iterator<ISegmentedVariantGraphVertex> iterator();

}
