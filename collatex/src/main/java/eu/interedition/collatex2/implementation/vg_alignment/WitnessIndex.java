package eu.interedition.collatex2.implementation.vg_alignment;

import java.util.List;

public class WitnessIndex implements IWitnessIndex {
  private final List<ITokenSequence> tokenSequences;

  public WitnessIndex(List<ITokenSequence> tokenSequences) {
    this.tokenSequences = tokenSequences;
  }

  @Override
  public List<ITokenSequence> getTokenSequences() {
    return tokenSequences;
  }
}
