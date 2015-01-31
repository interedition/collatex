package eu.interedition.collatex.dekker;

import java.util.Comparator;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.simple.SimpleToken;

public class SimpleTokenNormalizedFormComparator implements Comparator<Token> {

  @Override
  public int compare(Token o1, Token o2) {
    SimpleToken s1 = (SimpleToken) o1;
    SimpleToken s2 = (SimpleToken) o2;
    return s1.getNormalized().compareTo(s2.getNormalized());
  }

}
