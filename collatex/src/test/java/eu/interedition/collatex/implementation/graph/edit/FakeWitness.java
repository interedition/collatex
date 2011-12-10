package eu.interedition.collatex.implementation.graph.edit;

import eu.interedition.collatex.implementation.input.Witness;
import eu.interedition.collatex.implementation.input.SimpleToken;
import eu.interedition.collatex.interfaces.Token;

public class FakeWitness extends Witness {
  
  public FakeWitness() {
    super("fake");
  }
  
  public Token add(String content) {
    tokens.add(new SimpleToken(this, tokens.size(), content, content.toLowerCase()));
    prepareTokens();
    return new SimpleToken(this, tokens.size() - 1, content, content.toLowerCase());
  }
}
