package eu.interedition.collatex.matching;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.simple.SimpleToken;

import java.util.Comparator;

public class EqualityTokenComparator implements Comparator<Token> {

  @Override
  public int compare(Token base, Token witness) {
    final String baseContent = ((SimpleToken) base).getNormalized();
    final String witnessContent = ((SimpleToken) witness).getNormalized();
    return baseContent.compareTo(witnessContent);
  }

}
