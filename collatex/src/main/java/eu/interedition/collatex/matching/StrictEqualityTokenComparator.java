package eu.interedition.collatex.matching;

import java.util.Comparator;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.simple.SimpleToken;

public class StrictEqualityTokenComparator implements Comparator<Token> {

  @Override
  public int compare(Token base, Token witness) {
    final String baseContent = ((SimpleToken) base).getContent();
    final String witnessContent = ((SimpleToken) witness).getContent();
    return baseContent.compareTo(witnessContent);
  }

}
