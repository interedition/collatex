package eu.interedition.collatex.matching;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.input.SimpleToken;

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
