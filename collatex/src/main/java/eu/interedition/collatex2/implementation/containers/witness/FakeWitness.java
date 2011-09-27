package eu.interedition.collatex2.implementation.containers.witness;

import eu.interedition.collatex2.interfaces.INormalizedToken;

public class FakeWitness extends Witness {
  
  public FakeWitness() {
    super("fake");
  }
  
  public INormalizedToken add(String content) {
    String normalized = content.toLowerCase();
    INormalizedToken token = new WitnessToken(content, tokens.size(), normalized);
    tokens.add(token);
    return token;
  }



}
