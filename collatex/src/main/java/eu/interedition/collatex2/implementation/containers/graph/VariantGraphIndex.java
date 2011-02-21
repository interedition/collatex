package eu.interedition.collatex2.implementation.containers.graph;

import java.util.List;

import eu.interedition.collatex2.implementation.vg_alignment.AbstractTokenIndex;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

public class VariantGraphIndex extends AbstractTokenIndex {

  public VariantGraphIndex(IVariantGraph graph, List<String> repeatingTokens) {
    super();
    for (IWitness witness: graph.getWitnesses()) {
      List<INormalizedToken> tokens = graph.getTokens(witness);
      processTokens(tokens, repeatingTokens);
    }
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("VariantGraphIndex: (");
    String delimiter = "";
    for (final String normalizedPhrase : keys()) {
      result.append(delimiter).append(normalizedPhrase);
      delimiter = ", ";
    }
    result.append(")");
    return result.toString();
  }


}
