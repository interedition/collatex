package eu.interedition.collatex2.implementation.matching;

import com.google.common.collect.ListMultimap;

import eu.interedition.collatex2.implementation.vg_alignment.Superbase;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;


public class VariantGraphMatcher implements IVariantGraphMatcher {

  @Override
  public ListMultimap<INormalizedToken, INormalizedToken> match(IVariantGraph vGraph, IWitness b) {
    IWitness superbase = new Superbase(vGraph);
    TokenMatcher matcher = new TokenMatcher();
    ListMultimap<INormalizedToken, INormalizedToken> matches = matcher.match(superbase, b);
    return matches;
  }

}
