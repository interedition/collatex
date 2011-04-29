package eu.interedition.collatex2.experimental;

import eu.interedition.collatex2.implementation.CollateXEngine;

public class MyNewCollateXEngine extends CollateXEngine {
  public MyNewCollateXEngine() {
    super();
    setTokenizer(new WhitespaceAndPunctuationTokenizer());
  }

}
