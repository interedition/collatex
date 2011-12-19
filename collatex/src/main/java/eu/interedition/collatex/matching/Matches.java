package eu.interedition.collatex.matching;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.graph.VariantGraphVertex;
import eu.interedition.collatex.input.SimpleToken;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

public class Matches {

  private final ListMultimap<Token, VariantGraphVertex> all;
  private final Set<Token> unmatched;
  private final Set<Token> ambiguous;
  private final Set<Token> unique;

  public static Matches between(final Iterable<VariantGraphVertex> vertices, final Iterable<Token> witnessTokens, Comparator<Token> comparator) {

    final ListMultimap<Token, VariantGraphVertex> all = ArrayListMultimap.create();
    for (VariantGraphVertex vertex : vertices) {
      final SortedSet<Token> tokens = vertex.tokens();
      if (tokens.isEmpty()) {
        continue;
      }
      for (Token witnessToken : witnessTokens) {
        if (comparator.compare(tokens.first(), witnessToken) == 0) {
          all.put(witnessToken, vertex);
        }
      }
    }

    // unmatched tokens
    Set<Token> unmatched = Sets.newLinkedHashSet();
    for (Token witnessToken : witnessTokens) {
      if (!all.containsKey(witnessToken)) {
        unmatched.add(witnessToken);
      }
    }
    // unsure tokens (have to check: base -> witness, and witness -> base)
    Set<Token> ambiguous = Sets.newLinkedHashSet();
    for (Token witnessToken : witnessTokens) {
      int count = all.keys().count(witnessToken);
      if (count > 1) {
        ambiguous.add(witnessToken);
      }
    }
    Multiset<VariantGraphVertex> bag = ImmutableMultiset.copyOf(all.values());
    Set<VariantGraphVertex> unsureBaseTokens = Sets.newLinkedHashSet();
    for (VariantGraphVertex baseToken : vertices) {
      int count = bag.count(baseToken);
      if (count > 1) {
        unsureBaseTokens.add(baseToken);
      }
    }
    Collection<Map.Entry<Token, VariantGraphVertex>> entries = all.entries();
    for (Map.Entry<Token, VariantGraphVertex> entry : entries) {
      if (unsureBaseTokens.contains(entry.getValue())) {
        ambiguous.add(entry.getKey());
      }
    }
    // sure tokens
    // have to check unsure tokens because of (base -> witness && witness -> base)
    Set<Token> unique = Sets.newLinkedHashSet();
    for (Token witnessToken : witnessTokens) {
      if (all.keys().count(witnessToken) == 1 && !ambiguous.contains(witnessToken)) {
        unique.add(witnessToken);
      }
    }

    // add start and end tokens as matches
    all.put(SimpleToken.START, Iterables.getFirst(vertices, null));
    all.put(SimpleToken.END, Iterables.getLast(vertices));

    return new Matches(all, unmatched, ambiguous, unique);
  }

  private Matches(ListMultimap<Token, VariantGraphVertex> all, Set<Token> unmatched, Set<Token> ambiguous, Set<Token> unique) {
    this.all = all;
    this.unmatched = unmatched;
    this.ambiguous = ambiguous;
    this.unique = unique;
  }

  public ListMultimap<Token, VariantGraphVertex> getAll() {
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
