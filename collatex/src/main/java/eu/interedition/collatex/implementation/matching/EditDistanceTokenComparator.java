package eu.interedition.collatex.implementation.matching;

import eu.interedition.collatex.implementation.alignment.VariantGraphWitnessAdapter;
import eu.interedition.collatex.implementation.input.SimpleToken;
import eu.interedition.collatex.interfaces.Token;

import java.util.Comparator;

public class EditDistanceTokenComparator implements Comparator<Token> {

  private final int threshold;

  public EditDistanceTokenComparator() {
    this(1);
  }

  public EditDistanceTokenComparator(int threshold) {
    this.threshold = threshold;
  }

  @Override
  public int compare(Token base, Token witness) {
    final String baseContent = (base instanceof SimpleToken ? ((SimpleToken) base).getNormalized() : ((VariantGraphWitnessAdapter.VariantGraphVertexTokenAdapter) base).getNormalized());
    final String witnessContent = (witness instanceof SimpleToken ? ((SimpleToken) witness).getNormalized() : ((VariantGraphWitnessAdapter.VariantGraphVertexTokenAdapter) witness).getNormalized());
    return (EditDistance.compute(baseContent, witnessContent) <= threshold) ? 0 : -1;
  }
}
