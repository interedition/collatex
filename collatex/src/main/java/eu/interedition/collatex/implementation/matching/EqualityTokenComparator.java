package eu.interedition.collatex.implementation.matching;

import eu.interedition.collatex.implementation.input.SimpleToken;
import eu.interedition.collatex.interfaces.Token;

import java.util.Comparator;

public class EqualityTokenComparator implements Comparator<Token> {

  @Override
  public int compare(Token base, Token witness) {
    final String baseContent = ((SimpleToken) base).getNormalized();
    final String witnessContent = ((SimpleToken) witness).getNormalized();
    return baseContent.compareTo(witnessContent);
  }

}
