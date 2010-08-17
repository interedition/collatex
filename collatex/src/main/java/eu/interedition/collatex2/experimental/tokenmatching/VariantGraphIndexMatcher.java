package eu.interedition.collatex2.experimental.tokenmatching;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.interedition.collatex2.implementation.indexing.WitnessIndex;
import eu.interedition.collatex2.interfaces.ITokenContainer;
import eu.interedition.collatex2.interfaces.ITokenMatch;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.IWitnessIndex;

//TODO: this class is very similar to GenericTokenIndexMatcher!
//TODO: remove one or the other!
public class VariantGraphIndexMatcher implements ITokenMatcher {
  private final ITokenContainer graph;
  private IWitnessIndex graphIndex;

  public VariantGraphIndexMatcher(ITokenContainer graph) {
    this.graph = graph;
  }

  //TODO: change into Set?
  public List<ITokenMatch> getMatches(IWitness witness) {
    Set<String> repeatingTokens = Sets.newLinkedHashSet();
    repeatingTokens.addAll(graph.findRepeatingTokens());
    repeatingTokens.addAll(witness.findRepeatingTokens());
    List<String> repeatingTokensList = Lists.newArrayList(repeatingTokens);
    IWitnessIndex witnessIndex = new WitnessIndex(witness, repeatingTokensList);
    graphIndex = graph.getTokenIndex(repeatingTokensList);
    return IndexMatcher.findMatches(graphIndex, witnessIndex);
  }
}
