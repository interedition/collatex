package eu.interedition.collatex2.implementation.graph.edit;

import eu.interedition.collatex2.implementation.input.Witness;
import eu.interedition.collatex2.implementation.input.NormalizedToken;
import eu.interedition.collatex2.interfaces.INormalizedToken;

public class FakeWitness extends Witness {
  
  public FakeWitness() {
    super("fake");
  }
  
  public INormalizedToken add(String content) {
    String normalized = content.toLowerCase();
    INormalizedToken token = new NormalizedToken(content, normalized);
    tokens.add(token);
    prepareTokens();
    return token;
  }



}
