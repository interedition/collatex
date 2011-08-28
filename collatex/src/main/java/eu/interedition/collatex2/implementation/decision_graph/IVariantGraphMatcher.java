package eu.interedition.collatex2.implementation.decision_graph;

import com.google.common.collect.ListMultimap;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

public interface IVariantGraphMatcher {

  ListMultimap<INormalizedToken, INormalizedToken> match(IVariantGraph vGraph, IWitness b);

}
