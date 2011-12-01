package eu.interedition.collatex.implementation.graph.edit;

import eu.interedition.collatex.implementation.input.Witness;
import eu.interedition.collatex.implementation.input.NormalizedToken;
import eu.interedition.collatex.interfaces.INormalizedToken;

public class FakeWitness extends Witness {
  
  public FakeWitness() {
    super("fake");
  }
  
  public INormalizedToken add(String content) {
    tokens.add(new NormalizedToken(this, tokens.size(), content, content.toLowerCase()));
    prepareTokens();
    return new NormalizedToken(this, tokens.size() - 1, content, content.toLowerCase());
  }
}
