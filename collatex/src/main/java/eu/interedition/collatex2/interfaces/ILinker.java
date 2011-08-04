package eu.interedition.collatex2.interfaces;

import java.util.Map;


public interface ILinker {

  Map<INormalizedToken, INormalizedToken> link(IVariantGraph graph, IWitness b);

}