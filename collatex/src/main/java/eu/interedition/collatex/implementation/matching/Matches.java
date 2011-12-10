package eu.interedition.collatex.implementation.matching;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import eu.interedition.collatex.implementation.input.NormalizedToken;
import eu.interedition.collatex.interfaces.INormalizedToken;
import eu.interedition.collatex.interfaces.IWitness;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Matches {

  private final ListMultimap<INormalizedToken, INormalizedToken> all;
  private final Set<INormalizedToken> unmatched;
  private final Set<INormalizedToken> ambiguous;
  private final Set<INormalizedToken> unique;

  public static Matches between(final IWitness a, final IWitness b, Comparator<INormalizedToken> comparator) {
    final Iterable<INormalizedToken> aTokens = a.getTokens();
    final Iterable<INormalizedToken> bTokens = b.getTokens();

    final ListMultimap<INormalizedToken, INormalizedToken> all = ArrayListMultimap.create();
    for (INormalizedToken tokenA : aTokens) {
      for (INormalizedToken tokenB : bTokens) {
        if (comparator.compare(tokenA, tokenB) == 0) {
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
    Set<INormalizedToken> unsureBaseTokens = Sets.newLinkedHashSet();
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
    for (INormalizedToken token : bTokens) {
      if (all.keys().count(token) == 1 && !ambiguous.contains(token)) {
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
