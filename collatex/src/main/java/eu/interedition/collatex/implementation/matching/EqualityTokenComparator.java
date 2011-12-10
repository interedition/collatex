package eu.interedition.collatex.implementation.matching;

import eu.interedition.collatex.interfaces.Token;

import java.util.Comparator;

public class EqualityTokenComparator implements Comparator<Token> {

  @Override
  public int compare(Token base, Token witness) {
    return base.getNormalized().compareTo(witness.getNormalized());
  }

}
