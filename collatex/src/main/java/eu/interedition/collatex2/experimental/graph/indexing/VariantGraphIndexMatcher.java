package eu.interedition.collatex2.experimental.graph.indexing;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

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
  public List<ITokenMatch> getMatches(IWitness witness) {
    Set<String> repeatingTokens = Sets.newLinkedHashSet();
    repeatingTokens.addAll(graph.findRepeatingTokens());
    repeatingTokens.addAll(witness.findRepeatingTokens());
    // System.out.println(repeatingTokens);
    //TODO: change into Set?
    List<String> repeatingTokensList = Lists.newArrayList(repeatingTokens);
    IWitnessIndex witnessIndex = new WitnessIndex(witness, repeatingTokensList);
    graphIndex = VariantGraphIndex.create(graph, repeatingTokensList);
    // System.out.println(graphIndex.keys());
    // System.out.println(witnessIndex.keys());
    return IndexMatcher.findMatches(graphIndex, witnessIndex);
  }
  
  public IVariantGraphIndex getGraphIndex() {
    return graphIndex;
  }
}
