package eu.interedition.collatex2.implementation.containers.witness;

import java.util.List;

import eu.interedition.collatex2.implementation.vg_alignment.AbstractTokenIndex;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class WitnessIndex extends AbstractTokenIndex {

  public WitnessIndex(final IWitness witness, final List<String> repeatingTokens) {
    final List<INormalizedToken> tokens = witness.getTokens();
    processTokens(tokens, repeatingTokens);
  }


}
