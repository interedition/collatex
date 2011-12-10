package eu.interedition.collatex.implementation.matching;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import eu.interedition.collatex.implementation.input.SimpleToken;
import eu.interedition.collatex.interfaces.Token;
import eu.interedition.collatex.interfaces.IWitness;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

public class Matches {

  private final ListMultimap<Token, Token> all;
  private final Set<Token> unmatched;
  private final Set<Token> ambiguous;
  private final Set<Token> unique;

  public static Matches between(final IWitness a, final IWitness b, Comparator<Token> comparator) {
    final Iterable<Token> aTokens = a.getTokens();
    final Iterable<Token> bTokens = b.getTokens();

    final ListMultimap<Token, Token> all = ArrayListMultimap.create();
    for (Token tokenA : aTokens) {
      for (Token tokenB : bTokens) {
        if (comparator.compare(tokenA, tokenB) == 0) {
          all.put(tokenB, tokenA);
        }
      }
    }

    // unmatched tokens
    Set<Token> unmatched = Sets.newLinkedHashSet();
    for (Token token : bTokens) {
      if (!all.containsKey(token)) {
        unmatched.add(token);
      }
    }
    // unsure tokens (have to check: base -> witness, and witness -> base)
    Set<Token> ambiguous = Sets.newLinkedHashSet();
    for (Token token : bTokens) {
      int count = all.keys().count(token);
      if (count > 1) {
        ambiguous.add(token);
      }
    }
    Multiset<Token> bag = ImmutableMultiset.copyOf(all.values());
    Set<Token> unsureBaseTokens = Sets.newLinkedHashSet();
    for (Token token : aTokens) {
      int count = bag.count(token);
      if (count > 1) {
        unsureBaseTokens.add(token);
      }
    }
    Collection<Map.Entry<Token, Token>> entries = all.entries();
    for (Map.Entry<Token, Token> entry : entries) {
      if (unsureBaseTokens.contains(entry.getValue())) {
        ambiguous.add(entry.getKey());
      }
    }
    // sure tokens
    // have to check unsure tokens because of (base -> witness && witness -> base)
    Set<Token> unique = Sets.newLinkedHashSet();
    for (Token token : bTokens) {
      if (all.keys().count(token) == 1 && !ambiguous.contains(token)) {
        unique.add(token);
      }
    }

    // add start and end tokens as matches
    all.put(SimpleToken.START, Iterables.getFirst(aTokens, null));
    all.put(SimpleToken.END, Iterables.getLast(aTokens));

    return new Matches(all, unmatched, ambiguous, unique);
  }

  private Matches(ListMultimap<Token, Token> all, Set<Token> unmatched, Set<Token> ambiguous, Set<Token> unique) {
    this.all = all;
    this.unmatched = unmatched;
    this.ambiguous = ambiguous;
    this.unique = unique;
  }

  public ListMultimap<Token, Token> getAll() {
    return all;
  }

  public Set<Token> getUnmatched() {
    return unmatched;
  }

  public Set<Token> getAmbiguous() {
    return ambiguous;
  }

  public Set<Token> getUnique() {
    return unique;
  }

}
