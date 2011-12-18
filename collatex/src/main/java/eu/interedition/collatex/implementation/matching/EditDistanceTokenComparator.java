package eu.interedition.collatex.implementation.matching;

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
    final String baseContent = ((SimpleToken) base).getNormalized();
    final String witnessContent = ((SimpleToken) witness).getNormalized();
    return (EditDistance.compute(baseContent, witnessContent) <= threshold) ? 0 : -1;
  }
}
