package eu.interedition.collatex2.implementation.matching;

import java.util.Comparator;
import java.util.Iterator;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import com.google.common.collect.Multimap;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.ITokenContainer;
import eu.interedition.collatex2.interfaces.IWitness;

public abstract class TokenMatcher implements Comparator<INormalizedToken> {

  public Multimap<INormalizedToken, INormalizedToken> match(ITokenContainer a, ITokenContainer b) {
    final Multimap<INormalizedToken, INormalizedToken> matches = ArrayListMultimap.create();

    for (Iterator<INormalizedToken> aIt = a.tokenIterator(); aIt.hasNext(); ) {
      final INormalizedToken tokenA = aIt.next();
      for (Iterator<INormalizedToken> bIt = b.tokenIterator(); bIt.hasNext(); ) {
        final INormalizedToken tokenB = bIt.next();
        if (compare(tokenA, tokenB)== 0) {
          matches.put(tokenB, tokenA);
        }
      }
    }
    return matches;
  }
}
