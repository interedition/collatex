package eu.interedition.collatex2.experimental;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.vg_alignment.VariantGraphAligner;
import eu.interedition.collatex2.interfaces.IAligner;
import eu.interedition.collatex2.interfaces.IVariantGraph;

public class MyNewCollateXEngine extends CollateXEngine {
  public MyNewCollateXEngine() {
    super();
    setTokenizer(new WhitespaceAndPunctuationTokenizer());
  }
  
  @Override
  public IAligner createAligner(IVariantGraph graph) {
    return new VariantGraphAligner(graph);
  }
}
