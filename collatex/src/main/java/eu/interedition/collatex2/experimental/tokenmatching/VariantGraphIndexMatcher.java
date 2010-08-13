package eu.interedition.collatex2.experimental.tokenmatching;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.interedition.collatex2.experimental.graph.IVariantGraph;
import eu.interedition.collatex2.experimental.graph.indexing.IVariantGraphIndex;
import eu.interedition.collatex2.experimental.graph.indexing.VariantGraphIndex;
import eu.interedition.collatex2.implementation.indexing.WitnessIndex;
import eu.interedition.collatex2.interfaces.ITokenMatch;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.IWitnessIndex;


//TODO: this TokenMatcher could be made more generic if a
//TODO: ITokenContainer interface was introduced!
public class VariantGraphIndexMatcher implements ITokenMatcher {
  private final IVariantGraph graph;
  private IVariantGraphIndex graphIndex;

  public VariantGraphIndexMatcher(IVariantGraph graph) {
    this.graph = graph;
  }

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
}
