package eu.interedition.collatex2.implementation.edit_graph;

import com.google.common.collect.ListMultimap;

import eu.interedition.collatex2.implementation.matching.TokenMatcher;
import eu.interedition.collatex2.implementation.vg_alignment.SuperbaseCreator;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;


public class VariantGraphMatcher implements IVariantGraphMatcher {

  @Override
  public ListMultimap<INormalizedToken, INormalizedToken> match(IVariantGraph vGraph, IWitness b) {
    SuperbaseCreator creator = new SuperbaseCreator();
    IWitness superbase = creator.create(vGraph);
    TokenMatcher matcher = new TokenMatcher();
    ListMultimap<INormalizedToken, INormalizedToken> matches = matcher.match(superbase, b);
    return matches;
  }

}
