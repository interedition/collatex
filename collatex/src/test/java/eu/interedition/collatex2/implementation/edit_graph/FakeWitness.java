package eu.interedition.collatex2.implementation.edit_graph;

import eu.interedition.collatex2.implementation.containers.witness.Witness;
import eu.interedition.collatex2.implementation.containers.witness.WitnessToken;
import eu.interedition.collatex2.interfaces.INormalizedToken;

public class FakeWitness extends Witness {
  
  public FakeWitness() {
    super("fake");
  }
  
  public INormalizedToken add(String content) {
    String normalized = content.toLowerCase();
    INormalizedToken token = new WitnessToken(content, normalized);
    tokens.add(token);
    prepareTokens();
    return token;
  }



}
