package eu.interedition.collatex2.experimental.graph.indexing;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.experimental.graph.IVariantGraph;
import eu.interedition.collatex2.implementation.indexing.WitnessIndex;
import eu.interedition.collatex2.implementation.matching.IndexMatcher;
import eu.interedition.collatex2.interfaces.ITokenMatch;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.IWitnessIndex;

public class VariantGraphIndexMatcher {
  private final IVariantGraph graph;
  private IVariantGraphIndex graphIndex;

  //TODO: extract ITokenMatcher interface?
  public VariantGraphIndexMatcher(IVariantGraph graph) {
    this.graph = graph;
  }

  //TODO: do inversion of control for creation of indexes!!
  //TODO: take care of repeating tokens (same as IndexMatcher)!
  public List<ITokenMatch> getMatches(IWitness witness) {
    List<String> repeatingTokens = Lists.newArrayList();
    IWitnessIndex witnessIndex = new WitnessIndex(witness, repeatingTokens);
    graphIndex = VariantGraphIndex.create(graph, repeatingTokens);
    return IndexMatcher.findMatches(graphIndex, witnessIndex);
  }
  
  public IVariantGraphIndex getGraphIndex() {
    return graphIndex;
  }
}
