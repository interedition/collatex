package eu.interedition.collatex2.interfaces;

import java.util.Iterator;

public interface ITokenContainer {

  boolean isNear(IToken a, IToken b);

  Iterator<INormalizedToken> tokenIterator();

}
