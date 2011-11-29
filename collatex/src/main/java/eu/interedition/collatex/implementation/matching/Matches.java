package eu.interedition.collatex.implementation.matching;

import java.util.*;

import com.google.common.collect.*;
import eu.interedition.collatex.implementation.input.NormalizedToken;
import eu.interedition.collatex.interfaces.INormalizedToken;
import eu.interedition.collatex.interfaces.ITokenContainer;

public class Matches {

  private final ListMultimap<INormalizedToken, INormalizedToken> all;
  private final Set<INormalizedToken> unmatched;
  private final Set<INormalizedToken> ambiguous;
  private final Set<INormalizedToken> unique;

  public static Matches between(final ITokenContainer a, final ITokenContainer b, Comparator<INormalizedToken> comparator) {
    final Iterable<INormalizedToken> aTokens = new Iterable<INormalizedToken>() {
      @Override
      public Iterator<INormalizedToken> iterator() {
        return a.tokenIterator();
      }
    };
    final Iterable<INormalizedToken> bTokens = new Iterable<INormalizedToken>() {
      @Override
      public Iterator<INormalizedToken> iterator() {
        return b.tokenIterator();
      }
    };

    final ListMultimap<INormalizedToken, INormalizedToken> all = ArrayListMultimap.create();
    for (INormalizedToken tokenA : aTokens) {
      for (INormalizedToken tokenB : bTokens) {
        if (comparator.compare(tokenA, tokenB)== 0) {
          all.put(tokenB, tokenA);
        }
      }
    }

    // unmatched tokens
    Set<INormalizedToken> unmatched = Sets.newLinkedHashSet();
    for (INormalizedToken token : bTokens) {
      if (!all.containsKey(token)) {
        unmatched.add(token);
      }
    }
    // unsure tokens (have to check: base -> witness, and witness -> base)
    Set<INormalizedToken> ambiguous = Sets.newLinkedHashSet();
    for (INormalizedToken token : bTokens) {
      int count = all.keys().count(token);
      if (count > 1) {
        ambiguous.add(token);
      }
    }
    Multiset<INormalizedToken> bag = ImmutableMultiset.copyOf(all.values());
    Set<INormalizedToken> unsureBaseTokens =  Sets.newLinkedHashSet();
    for (INormalizedToken token : aTokens) {
      int count = bag.count(token);
      if (count > 1) {
        unsureBaseTokens.add(token);
      }
    }
    Collection<Map.Entry<INormalizedToken, INormalizedToken>> entries = all.entries();
    for (Map.Entry<INormalizedToken, INormalizedToken> entry : entries) {
      if (unsureBaseTokens.contains(entry.getValue())) {
        ambiguous.add(entry.getKey());
      }
    }
    // sure tokens
    // have to check unsure tokens because of (base -> witness && witness -> base)
    Set<INormalizedToken> unique = Sets.newLinkedHashSet();
    for (INormalizedToken token: bTokens) {
      if (all.keys().count(token)==1&&!ambiguous.contains(token)) {
        unique.add(token);
      }
    }

    // add start and end tokens as matches
    all.put(NormalizedToken.START, Iterables.getFirst(aTokens, null));
    all.put(NormalizedToken.END, Iterables.getLast(aTokens));

    return new Matches(all, unmatched, ambiguous, unique);
  }

  private Matches(ListMultimap<INormalizedToken, INormalizedToken> all, Set<INormalizedToken> unmatched, Set<INormalizedToken> ambiguous, Set<INormalizedToken> unique) {
    this.all = all;
    this.unmatched = unmatched;
    this.ambiguous = ambiguous;
    this.unique = unique;
  }

  public ListMultimap<INormalizedToken, INormalizedToken> getAll() {
    return all;
  }

  public Set<INormalizedToken> getUnmatched() {
    return unmatched;
  }

  public Set<INormalizedToken> getAmbiguous() {
    return ambiguous;
  }

  public Set<INormalizedToken> getUnique() {
    return unique;
  }

}
