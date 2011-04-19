package eu.interedition.collatex2.experimental;

import java.util.List;

public class NewWitnessIndex implements IWitnessIndex {
  private final List<ITokenSequence> tokenSequences;

  public NewWitnessIndex(List<ITokenSequence> tokenSequences) {
    this.tokenSequences = tokenSequences;
  }

  @Override
  public List<ITokenSequence> getTokenSequences() {
    return tokenSequences;
  }
}
